package com.yash.speachr

import android.app.Application
import com.yash.speachr.core.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class SpeachrApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@SpeachrApplication)
            modules(appModule)
        }
    }
}
