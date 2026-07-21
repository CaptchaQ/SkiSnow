package com.skisnow.data.db

import com.skisnow.domain.model.LocationSource
import com.skisnow.domain.model.SessionId
import com.skisnow.domain.model.SessionStats
import com.skisnow.domain.model.SessionStatus
import com.skisnow.domain.model.SkiDay
import com.skisnow.domain.model.TrackPoint
import java.time.Duration
import java.time.Instant

fun SkiDayEntity.toDomain(): SkiDay = SkiDay(
    id = SessionId(id),
    startedAt = Instant.ofEpochMilli(startedAtEpochMs),
    endedAt = endedAtEpochMs?.let(Instant::ofEpochMilli),
    status = SessionStatus.valueOf(status),
)

fun SkiDay.toEntity(): SkiDayEntity = SkiDayEntity(
    id = id.value,
    startedAtEpochMs = startedAt.toEpochMilli(),
    endedAtEpochMs = endedAt?.toEpochMilli(),
    status = status.name,
)

fun TrackPointEntity.toDomain(): TrackPoint = TrackPoint(
    time = Instant.ofEpochMilli(timeEpochMs),
    lat = lat,
    lon = lon,
    altitudeM = altitudeM,
    speedMps = speedMps,
    accuracyM = accuracyM,
    source = runCatching { LocationSource.valueOf(source) }.getOrDefault(LocationSource.UNKNOWN),
)

fun TrackPoint.toEntity(sessionId: String): TrackPointEntity = TrackPointEntity(
    sessionId = sessionId,
    timeEpochMs = time.toEpochMilli(),
    lat = lat,
    lon = lon,
    altitudeM = altitudeM,
    speedMps = speedMps,
    accuracyM = accuracyM,
    source = source.name,
)

fun SessionStatsEntity.toDomain(): SessionStats = SessionStats(
    duration = Duration.ofMillis(durationMs),
    distanceM = distanceM,
    verticalDropM = verticalDropM,
    maxSpeedMps = maxSpeedMps,
    avgMovingSpeedMps = avgMovingSpeedMps,
)

fun SessionStats.toEntity(sessionId: String): SessionStatsEntity = SessionStatsEntity(
    sessionId = sessionId,
    durationMs = duration.toMillis(),
    distanceM = distanceM,
    verticalDropM = verticalDropM,
    maxSpeedMps = maxSpeedMps,
    avgMovingSpeedMps = avgMovingSpeedMps,
)
