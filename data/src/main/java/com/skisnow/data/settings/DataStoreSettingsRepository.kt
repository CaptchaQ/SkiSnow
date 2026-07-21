package com.skisnow.data.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.skisnow.domain.model.Units
import com.skisnow.domain.model.UserSettings
import com.skisnow.domain.port.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "skisnow_settings")

class DataStoreSettingsRepository(
    private val appContext: Context,
) : SettingsRepository {

    override fun observeSettings(): Flow<UserSettings> =
        appContext.settingsDataStore.data.map { prefs -> prefs.toSettings() }

    override suspend fun getSettings(): UserSettings =
        appContext.settingsDataStore.data.map { prefs -> prefs.toSettings() }.let {
            var value: UserSettings = UserSettings()
            it.collect { s -> value = s; return@collect }
            value
        }

    override suspend fun setUnits(units: Units) {
        appContext.settingsDataStore.edit { it[KEY_UNITS] = units.name }
    }

    private fun Preferences.toSettings(): UserSettings {
        val raw = this[KEY_UNITS] ?: Units.METRIC.name
        val units = runCatching { Units.valueOf(raw) }.getOrDefault(Units.METRIC)
        return UserSettings(units = units)
    }

    private companion object {
        val KEY_UNITS = stringPreferencesKey("units")
    }
}