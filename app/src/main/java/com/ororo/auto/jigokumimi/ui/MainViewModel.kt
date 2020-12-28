package com.ororo.auto.jigokumimi.ui

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.repository.IDeviceRepository
import com.ororo.auto.jigokumimi.ui.common.BaseAndroidViewModel
import com.spotify.sdk.android.auth.AuthorizationClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get

class MainViewModel(
    private val app: Application,
    authRepository: IAuthRepository,
    private val deviceRepository: IDeviceRepository
) : BaseAndroidViewModel(app, authRepository) {

    private val _isOnline = MutableStateFlow(false)
     val isOnline : StateFlow<Boolean> get() = _isOnline

    init {
        observeNetworkConnection()
    }

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

    /**
     * 通信状態を監視
     */
    private fun observeNetworkConnection() {
        viewModelScope.launch {
            deviceRepository.observeNetworkConnection().collect {
                _isOnline.value = it
            }
        }
    }
}