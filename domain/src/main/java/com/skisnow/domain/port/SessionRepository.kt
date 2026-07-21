package com.skisnow.domain.port

import com.skisnow.domain.model.SessionId
import com.skisnow.domain.model.SessionStats
import com.skisnow.domain.model.SessionStatus
import com.skisnow.domain.model.SkiDay
import com.skisnow.domain.model.TrackPoint
import java.time.Instant
import kotlinx.coroutines.flow.Flow

/**
 * Local-first session store. Implementation lives in data layer (Room later).
 */
interface SessionRepository {
    suspend fun createSession(startedAt: Instant = Instant.now()): SkiDay

    suspend fun getSession(id: SessionId): SkiDay?

    suspend fun updateStatus(id: SessionId, status: SessionStatus, endedAt: Instant? = null)

    suspend fun appendTrackPoints(id: SessionId, points: List<TrackPoint>)

    suspend fun getTrackPoints(id: SessionId): List<TrackPoint>

    suspend fun saveStats(id: SessionId, stats: SessionStats)

    suspend fun getStats(id: SessionId): SessionStats?

    fun observeActiveSession(): Flow<SkiDay?>

    fun observeHistory(): Flow<List<SkiDay>>

    suspend fun getActiveSession(): SkiDay?
}
