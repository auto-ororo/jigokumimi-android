package com.ororo.auto.jigokumimi.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ororo.auto.jigokumimi.JigokumimiApplication
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import kotlinx.coroutines.launch

class MainViewModel(
    application: Application,
    private val authRepository: IAuthRepository
) :
    BaseAndroidViewModel(application) {

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
     * Factoryクラス
     */
    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(
                    app,
                    (app.applicationContext as JigokumimiApplication).authRepository
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

}