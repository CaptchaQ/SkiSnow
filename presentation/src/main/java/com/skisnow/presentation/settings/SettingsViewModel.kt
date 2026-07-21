package com.skisnow.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skisnow.domain.model.Units
import com.skisnow.domain.model.UserSettings
import com.skisnow.domain.port.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settings: SettingsRepository,
) : ViewModel() {

    val state: StateFlow<UserSettings> = settings.observeSettings()
        .stateIn(viewModelScope, SharingStarted.Lazily, UserSettings())

    fun setUnits(units: Units) {
        viewModelScope.launch { settings.setUnits(units) }
    }
}