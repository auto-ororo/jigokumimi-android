package com.ororo.auto.jigokumimi.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SplashViewModel(application: Application) : BaseAndroidViewModel(application) {

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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val (email, password) = authRepository.getSavedLoginInfo()
                if (email != "" && password != "") {
                    authRepository.loginJigokumimi(email, password)
                    _isLogin.postValue(true)
                } else {
                    _isLogin.postValue(false)

                }
            } catch (e: Exception) {
                _isLogin.postValue(false)
            } finally {
                _isReady.postValue(true)
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
                return SplashViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}