package com.skisnow.domain.port

import com.skisnow.domain.model.Units
import com.skisnow.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<UserSettings>
    suspend fun getSettings(): UserSettings
    suspend fun setUnits(units: Units)
}