package com.skisnow.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: SkiDayEntity)

    @Query("SELECT * FROM ski_days WHERE id = :id LIMIT 1")
    suspend fun getDay(id: String): SkiDayEntity?

    @Query("UPDATE ski_days SET status = :status, endedAtEpochMs = COALESCE(:endedAtEpochMs, endedAtEpochMs) WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, endedAtEpochMs: Long?)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPoints(points: List<TrackPointEntity>)

    @Query("SELECT * FROM track_points WHERE sessionId = :sessionId ORDER BY timeEpochMs ASC")
    suspend fun getPoints(sessionId: String): List<TrackPointEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: SessionStatsEntity)

    @Query("SELECT * FROM session_stats WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getStats(sessionId: String): SessionStatsEntity?

    @Query(
        """
        SELECT * FROM ski_days
        WHERE status IN ('RECORDING', 'PAUSED', 'STOPPING')
        ORDER BY startedAtEpochMs DESC
        LIMIT 1
        """,
    )
    suspend fun getActiveDay(): SkiDayEntity?

    @Query(
        """
        SELECT * FROM ski_days
        WHERE status IN ('RECORDING', 'PAUSED', 'STOPPING')
        ORDER BY startedAtEpochMs DESC
        LIMIT 1
        """,
    )
    fun observeActiveDay(): Flow<SkiDayEntity?>

    @Query("SELECT * FROM ski_days ORDER BY startedAtEpochMs DESC")
    fun observeHistory(): Flow<List<SkiDayEntity>>
}
