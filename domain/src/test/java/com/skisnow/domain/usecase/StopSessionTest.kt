package com.skisnow.domain.usecase

import com.skisnow.domain.model.LocationSource
import com.skisnow.domain.model.SessionId
import com.skisnow.domain.model.SessionStats
import com.skisnow.domain.model.SessionStatus
import com.skisnow.domain.model.SkiDay
import com.skisnow.domain.model.TrackPoint
import com.skisnow.domain.port.DefaultStatsCalculator
import com.skisnow.domain.port.LocationTracker
import com.skisnow.domain.port.SessionRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StopSessionTest {

    @Test
    fun stoppingSession_resumesFinalize_toSaved() = runBlocking {
        val id = SessionId("s1")
        val started = Instant.parse("2026-01-01T10:00:00Z")
        val repo = FakeSessionRepository(
            initial = SkiDay(id, started, null, SessionStatus.STOPPING),
            points = listOf(
                point(started, 45.0, 6.0, 2000.0, 10.0),
                point(started.plusSeconds(10), 45.001, 6.0, 1990.0, 12.0),
            ),
        )
        val tracker = FakeLocationTracker()
        val stop = StopSession(repo, DefaultStatsCalculator(), tracker)

        val result = stop(stopTracking = true)

        assertEquals(SessionStatus.SAVED, result.status)
        // Already STOPPING → do not call tracker.stop again (avoids FGS re-entry).
        assertEquals(0, tracker.stopCalls)
        assertTrue(repo.savedStats != null)
    }

    @Test
    fun recordingSession_stopsTracker_thenSaved() = runBlocking {
        val id = SessionId("s2")
        val started = Instant.parse("2026-01-01T10:00:00Z")
        val repo = FakeSessionRepository(
            initial = SkiDay(id, started, null, SessionStatus.RECORDING),
            points = listOf(point(started, 45.0, 6.0, 2000.0, 5.0)),
        )
        val tracker = FakeLocationTracker()
        val stop = StopSession(repo, DefaultStatsCalculator(), tracker)

        val result = stop(stopTracking = true)

        assertEquals(SessionStatus.SAVED, result.status)
        assertEquals(1, tracker.stopCalls)
    }

    @Test
    fun noActiveSession_throws() = runBlocking {
        val repo = FakeSessionRepository(
            initial = SkiDay(
                SessionId("s3"),
                Instant.parse("2026-01-01T10:00:00Z"),
                Instant.parse("2026-01-01T11:00:00Z"),
                SessionStatus.SAVED,
            ),
        )
        val stop = StopSession(repo, DefaultStatsCalculator(), FakeLocationTracker())
        try {
            stop()
            org.junit.Assert.fail("expected IllegalStateException")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("No active session"))
        }
    }

    private fun point(
        time: Instant,
        lat: Double,
        lon: Double,
        alt: Double,
        speed: Double,
    ) = TrackPoint(
        time = time,
        lat = lat,
        lon = lon,
        altitudeM = alt,
        speedMps = speed,
        accuracyM = 5.0,
        source = LocationSource.FUSED,
    )

    private class FakeLocationTracker : LocationTracker {
        var stopCalls = 0
        override fun observeTrackPoints() = flowOf<TrackPoint>()
        override fun start(sessionId: SessionId) = true
        override fun pause() = Unit
        override fun resume(sessionId: SessionId) = true
        override fun stop() {
            stopCalls++
        }
    }

    private class FakeSessionRepository(
        initial: SkiDay,
        private val points: List<TrackPoint> = emptyList(),
    ) : SessionRepository {
        private var day = initial
        var savedStats: SessionStats? = null
        private val activeFlow = MutableStateFlow(asActive(initial))

        private fun asActive(d: SkiDay): SkiDay? =
            d.takeIf {
                it.status == SessionStatus.RECORDING ||
                    it.status == SessionStatus.PAUSED ||
                    it.status == SessionStatus.STOPPING
            }

        override suspend fun createSession(startedAt: Instant) = error("unused")
        override suspend fun getSession(id: SessionId) = day.takeIf { it.id == id }
        override suspend fun updateStatus(id: SessionId, status: SessionStatus, endedAt: Instant?) {
            day = day.copy(status = status, endedAt = endedAt ?: day.endedAt)
            activeFlow.value = asActive(day)
        }
        override suspend fun appendTrackPoints(id: SessionId, points: List<TrackPoint>) = Unit
        override suspend fun getTrackPoints(id: SessionId) = points
        override suspend fun saveStats(id: SessionId, stats: SessionStats) {
            savedStats = stats
        }
        override suspend fun getStats(id: SessionId) = savedStats
        override fun observeActiveSession(): Flow<SkiDay?> = activeFlow
        override fun observeHistory(): Flow<List<SkiDay>> = flowOf(listOf(day))
        override suspend fun getActiveSession(): SkiDay? = asActive(day)
    }
}
