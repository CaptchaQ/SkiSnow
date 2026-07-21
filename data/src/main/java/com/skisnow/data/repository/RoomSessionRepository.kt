package com.skisnow.data.repository

import com.skisnow.data.db.SessionDao
import com.skisnow.data.db.toDomain
import com.skisnow.data.db.toEntity
import com.skisnow.domain.model.SessionId
import com.skisnow.domain.model.SessionStats
import com.skisnow.domain.model.SessionStatus
import com.skisnow.domain.model.SkiDay
import com.skisnow.domain.model.TrackPoint
import com.skisnow.domain.port.SessionRepository
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomSessionRepository(
    private val dao: SessionDao,
) : SessionRepository {

    override suspend fun createSession(startedAt: Instant): SkiDay {
        val day = SkiDay(
            id = SessionId(UUID.randomUUID().toString()),
            startedAt = startedAt,
            endedAt = null,
            status = SessionStatus.RECORDING,
        )
        dao.insertDay(day.toEntity())
        return day
    }

    override suspend fun getSession(id: SessionId): SkiDay? =
        dao.getDay(id.value)?.toDomain()

    override suspend fun updateStatus(
        id: SessionId,
        status: SessionStatus,
        endedAt: Instant?,
    ) {
        dao.updateStatus(id.value, status.name, endedAt?.toEpochMilli())
    }

    override suspend fun appendTrackPoints(id: SessionId, points: List<TrackPoint>) {
        if (points.isEmpty()) return
        dao.insertPoints(points.map { it.toEntity(id.value) })
    }

    override suspend fun getTrackPoints(id: SessionId): List<TrackPoint> =
        dao.getPoints(id.value).map { it.toDomain() }

    override suspend fun saveStats(id: SessionId, stats: SessionStats) {
        dao.insertStats(stats.toEntity(id.value))
    }

    override suspend fun getStats(id: SessionId): SessionStats? =
        dao.getStats(id.value)?.toDomain()

    override fun observeActiveSession(): Flow<SkiDay?> =
        dao.observeActiveDay().map { it?.toDomain() }

    override fun observeHistory(): Flow<List<SkiDay>> =
        dao.observeHistory().map { list -> list.map { it.toDomain() } }

    override suspend fun getActiveSession(): SkiDay? =
        dao.getActiveDay()?.toDomain()
}
