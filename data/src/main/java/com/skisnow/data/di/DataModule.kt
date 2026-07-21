package com.skisnow.data.di

import androidx.room.Room
import com.skisnow.data.db.SkiSnowDatabase
import com.skisnow.data.location.FusedLocationTracker
import com.skisnow.data.repository.RoomSessionRepository
import com.skisnow.data.settings.DataStoreSettingsRepository
import com.skisnow.data.weather.OpenMeteoWeatherRepository
import com.skisnow.domain.port.DefaultStatsCalculator
import com.skisnow.domain.port.LocationTracker
import com.skisnow.domain.port.SessionRepository
import com.skisnow.domain.port.SettingsRepository
import com.skisnow.domain.port.StatsCalculator
import com.skisnow.domain.port.WeatherRepository
import com.skisnow.domain.usecase.GetSessionDetail
import com.skisnow.domain.usecase.ObserveActiveSession
import com.skisnow.domain.usecase.ObserveSessionHistory
import com.skisnow.domain.usecase.PauseSession
import com.skisnow.domain.usecase.ResumeSession
import com.skisnow.domain.usecase.StartSession
import com.skisnow.domain.usecase.StopSession
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            SkiSnowDatabase::class.java,
            "skisnow.db",
        ).fallbackToDestructiveMigration().build()
    }
    single { get<SkiSnowDatabase>().sessionDao() }

    single<SessionRepository> { RoomSessionRepository(get()) }
    single<StatsCalculator> { DefaultStatsCalculator() }
    single<LocationTracker> { FusedLocationTracker(androidContext()) }
    single<WeatherRepository> { OpenMeteoWeatherRepository() }
    single<SettingsRepository> { DataStoreSettingsRepository(androidContext()) }

    factory { StartSession(get(), get()) }
    factory { PauseSession(get(), get()) }
    factory { ResumeSession(get(), get()) }
    factory { StopSession(get(), get(), get()) }
    factory { ObserveActiveSession(get()) }
    factory { ObserveSessionHistory(get()) }
    factory { GetSessionDetail(get()) }
}