package com.ororo.auto.jigokumimi

import android.app.Application
import com.ororo.auto.jigokumimi.util.koinModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class JigokumimiApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Timber初期化
        Timber.plant(Timber.DebugTree())

        startKoin {

            androidLogger(Level.DEBUG)

            androidContext(this@JigokumimiApplication)

            modules(koinModules)
        }
    }
}