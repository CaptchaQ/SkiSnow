package com.skisnow.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SkiDayEntity::class, TrackPointEntity::class, SessionStatsEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class SkiSnowDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
}
