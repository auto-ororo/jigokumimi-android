package com.ororo.auto.jigokumimi.ui.login

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import com.ororo.auto.jigokumimi.firebase.FirestoreService
import com.ororo.auto.jigokumimi.network.SpotifyApi
import com.ororo.auto.jigokumimi.repository.AuthRepository
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.repository.demo.DemoAuthRepository
import com.ororo.auto.jigokumimi.ui.common.BaseAndroidViewModel
import com.ororo.auto.jigokumimi.util.SingleLiveEvent
import com.ororo.auto.jigokumimi.util.demoRepositoryModule
import com.ororo.auto.jigokumimi.util.repositoryModule
import kotlinx.coroutines.launch
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
        authRepository = DemoAuthRepository()
    }

    fun doneAuthenticated() {
        _authenticated.call()
    }

    fun initRepository() {
        unloadKoinModules(demoRepositoryModule)
        unloadKoinModules(repositoryModule)
        loadKoinModules(repositoryModule)
        authRepository = AuthRepository(
            PreferenceManager.getDefaultSharedPreferences(app.applicationContext),
            SpotifyApi.retrofitService,
            FirestoreService(FirebaseFirestore.getInstance())
        )
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