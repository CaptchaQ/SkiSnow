package com.skisnow.presentation.di

import com.skisnow.presentation.session.SessionViewModel
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
}
