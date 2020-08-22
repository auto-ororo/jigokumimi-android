package com.ororo.auto.jigokumimi.ui

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.ui.common.BaseAndroidViewModel
import kotlinx.coroutines.launch

class MainViewModel(
    application: Application,
    authRepository: IAuthRepository
) :
    BaseAndroidViewModel(application, authRepository) {

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
}