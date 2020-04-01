package com.ororo.auto.jigokumimi

import android.app.Application
import com.ororo.auto.jigokumimi.repository.AuthRepository
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.repository.ILocationRepository
import com.ororo.auto.jigokumimi.repository.IMusicRepository
import timber.log.Timber

class JigokumimiApplication : Application() {

    val authRepository: IAuthRepository
        get() = ServiceLocator.getAuthRepository(this)

    val musicRepository: IMusicRepository
        get() = ServiceLocator.getMusicRepository(this)

    val locationRepository: ILocationRepository
        get() = ServiceLocator.getLocationRepository(this)

    override fun onCreate() {
        super.onCreate()

        // Timber初期化
        Timber.plant(Timber.DebugTree())
    }
}