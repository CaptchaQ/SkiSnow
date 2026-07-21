package com.skisnow.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "ski_days")
data class SkiDayEntity(
    @PrimaryKey val id: String,
    val startedAtEpochMs: Long,
    val endedAtEpochMs: Long?,
    val status: String,
)

@Entity(
    tableName = "track_points",
    primaryKeys = ["sessionId", "timeEpochMs"],
    foreignKeys = [
        ForeignKey(
            entity = SkiDayEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionId"), Index(value = ["sessionId", "timeEpochMs"])],
)
data class TrackPointEntity(
    val sessionId: String,
    val timeEpochMs: Long,
    val lat: Double,
    val lon: Double,
    val altitudeM: Double?,
    val speedMps: Double?,
    val accuracyM: Double?,
    val source: String,
)

@Entity(tableName = "session_stats")
data class SessionStatsEntity(
    @PrimaryKey val sessionId: String,
    val durationMs: Long,
    val distanceM: Double,
    val verticalDropM: Double,
    val maxSpeedMps: Double,
    val avgMovingSpeedMps: Double,
)
