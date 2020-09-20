package com.ororo.auto.jigokumimi.ui.login

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.ui.common.BaseAndroidViewModel
import com.ororo.auto.jigokumimi.util.SingleLiveEvent
import com.ororo.auto.jigokumimi.util.demoRepositoryModule
import com.ororo.auto.jigokumimi.util.repositoryModule
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

/**
 * ログイン画面のViewModel
 */
class LoginViewModel(private val app: Application, authRepository: IAuthRepository) :
    BaseAndroidViewModel(app, authRepository) {

    private var _authenticated = SingleLiveEvent<Unit>()
    val authenticated: LiveData<Unit>
        get() = _authenticated

    fun setDemoMode() {
        unloadKoinModules(repositoryModule)
        loadKoinModules(demoRepositoryModule)

        authRepository = app.get()
    }

    fun doneAuthenticated() {
        _authenticated.call()
    }

    fun refreshSpotifyAuthToken(token: String) {
        viewModelScope.launch {
            authRepository.refreshSpotifyAuthToken(token)
        }
    }

    fun createUserIfNeeded() {
        viewModelScope.launch {
            try {
                val spotifyUserId = authRepository.getSpotifyUserId()
                if (!authRepository.existsUser(spotifyUserId)) {
                    authRepository.createUser(spotifyUserId)
                }
            } catch (e: Exception) {
                handleAuthException(e)
            }
        }
    }
}