package com.skisnow.app

import android.app.Application
import com.skisnow.data.di.dataModule
import com.skisnow.presentation.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class SkiSnowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@SkiSnowApplication)
            modules(dataModule, presentationModule)
        }
    }
}
