package com.skisnow.presentation.di

import androidx.lifecycle.SavedStateHandle
import com.skisnow.presentation.detail.SessionDetailViewModel
import com.skisnow.presentation.session.SessionViewModel
import com.skisnow.presentation.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel {
        SessionViewModel(
            startSession = get(),
            pauseSession = get(),
            resumeSession = get(),
            stopSession = get(),
            observeActiveSession = get(),
            observeSessionHistory = get(),
            locationTracker = get(),
            sessionRepository = get(),
            statsCalculator = get(),
            weatherRepository = get(),
        )
    }
    viewModel { (sessionId: String) ->
        SessionDetailViewModel(
            savedStateHandle = SavedStateHandle().apply {
                set(SessionDetailViewModel.KEY_SESSION_ID, sessionId)
            },
            getSessionDetail = get(),
            statsCalculator = get(),
        )
    }
    viewModel { SettingsViewModel(get()) }
}