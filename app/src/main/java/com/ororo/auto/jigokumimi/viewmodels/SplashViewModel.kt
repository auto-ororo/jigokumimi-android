package com.ororo.auto.jigokumimi.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ororo.auto.jigokumimi.JigokumimiApplication
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import kotlinx.coroutines.launch

class SplashViewModel(application: Application, private val authRepository: IAuthRepository) :
    BaseAndroidViewModel(application) {

    /**
     *  初期化状態(Private)
     */
    private var _isReady = MutableLiveData(false)

    /**
     *  初期化状態状態
     */
    val isReady: MutableLiveData<Boolean>
        get() = _isReady

    /**
     *  ログイン状態(Private)
     */
    private var _isLogin = MutableLiveData(false)

    /**
     *  ログイン状態
     */
    val isLogin: MutableLiveData<Boolean>
        get() = _isLogin

    /**
     * 端末に保存されたログイン情報を元にログインを試みる
     */
    fun loginBySavedInput() {
        viewModelScope.launch {
            try {
                val (email, password) = authRepository.getSavedLoginInfo()
                if (email != "" && password != "") {
                    authRepository.loginJigokumimi(email, password)
                    _isLogin.value = true
                } else {
                    _isLogin.value = false

                }
            } catch (e: Exception) {
                _isLogin.value = false
            } finally {
                _isReady.value = true
            }

        }
    }

    /**
     * Factoryクラス
     */
    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SplashViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SplashViewModel(
                    app,
                    (app.applicationContext as JigokumimiApplication).authRepository
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}