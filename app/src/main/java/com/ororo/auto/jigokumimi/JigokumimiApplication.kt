package com.ororo.auto.jigokumimi

import android.app.Application
import timber.log.Timber

class JigokumimiApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Timber初期化
        Timber.plant(Timber.DebugTree())
    }
}