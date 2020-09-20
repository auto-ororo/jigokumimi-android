package com.ororo.auto.jigokumimi.ui

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.ui.common.BaseAndroidViewModel
import com.spotify.sdk.android.auth.AuthorizationClient
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get

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
                AuthorizationClient.clearCookies(app.applicationContext)
            } catch (e: Exception) {
                handleAuthException(e)
            }
        }
    }

    /**
     * Demo用AuthRepositoryをセット
     */
    fun setDemoAuthRepository() {
        authRepository = app.get()
    }

}