package com.ororo.auto.jigokumimi.ui

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.ororo.auto.jigokumimi.network.JigokumimiApi
import com.ororo.auto.jigokumimi.network.SpotifyApi
import com.ororo.auto.jigokumimi.repository.AuthRepository
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.repository.demo.DemoAuthRepository
import com.ororo.auto.jigokumimi.ui.common.BaseAndroidViewModel
import kotlinx.coroutines.launch

class MainViewModel(
    private val app: Application,
    authRepository: IAuthRepository
) :
    BaseAndroidViewModel(app, authRepository) {

    /**
     * ログアウト実行
     */
    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logoutJigokumimi()
            } catch (e: Exception) {
                handleAuthException(e)
            }
        }
    }

    /**
     * SpotifyAccessTokenを更新
     */
    fun refreshSpotifyAuthToken(token: String) {
        viewModelScope.launch {
            // Spotifyトークンを更新
            authRepository.refreshSpotifyAuthToken(token)
        }
    }

    /**
     * AuthRepositoryをセット
     */
    fun setAuthRepository() {
        authRepository = AuthRepository(
            PreferenceManager.getDefaultSharedPreferences(app.applicationContext),
            JigokumimiApi.retrofitService,
            SpotifyApi.retrofitService
        )
    }

    /**
     * Demo用AuthRepositoryをセット
     */
    fun setDemoAuthRepository() {
        authRepository = DemoAuthRepository(app)
    }

}