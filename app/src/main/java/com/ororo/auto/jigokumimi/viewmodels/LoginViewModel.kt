package com.ororo.auto.jigokumimi.viewmodels

import android.app.Application
import android.app.Service
import android.util.Patterns
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.ororo.auto.jigokumimi.JigokumimiApplication
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.network.JigokumimiApi
import com.ororo.auto.jigokumimi.network.SpotifyApi
import com.ororo.auto.jigokumimi.repository.AuthRepository
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.repository.LocationRepository
import com.ororo.auto.jigokumimi.repository.MusicRepository
import com.ororo.auto.jigokumimi.repository.demo.DemoAuthRepository
import com.ororo.auto.jigokumimi.repository.demo.DemoLocationRepository
import com.ororo.auto.jigokumimi.repository.demo.DemoMusicRepository
import kotlinx.coroutines.launch
import java.util.regex.Pattern

/**
 * ログイン画面のViewModel
 */
class LoginViewModel(val app: Application, authRepository: IAuthRepository) :
    BaseAndroidViewModel(app, authRepository) {

    /**
     *  ログイン状態
     */
    private var _isLogin = MutableLiveData(false)
    val isLogin: MutableLiveData<Boolean>
        get() = _isLogin

    /**
     *  デモモード状態
     */
    private var _isDemo = MutableLiveData(false)
    val isDemo: MutableLiveData<Boolean>
        get() = _isDemo

    /**
     * Emailの入力内容を保持
     */
    val email = MutableLiveData<String>()

    /**
     * Passwordの入力内容を保持
     */
    val password = MutableLiveData<String>()

    /**
     * ログインボタンの活性・非活性
     */
    private val _loginButtonEnabledState = MediatorLiveData<Boolean>()
    val loginButtonEnabledState: LiveData<Boolean>
        get() = _loginButtonEnabledState

    init {
        // Email,Passwordが変更された時にバリデーションを実行し、ログインボタンの活性・非活性制御を行う
        _loginButtonEnabledState.addSource(email) { validateLogin() }
        _loginButtonEnabledState.addSource(password) { validateLogin() }
    }

    /**
     * 画面全体のバリデーション
     */
    private fun validateLogin() {
        _loginButtonEnabledState.value = validateEmail() && validatePassword()
    }

    /**
     * Emailのバリデーション
     * メールアドレスの形式で、空文字以外を許可
     */
    private fun validateEmail() = Patterns.EMAIL_ADDRESS.matcher(email.value ?: "").matches()

    /**
     * Passwordのバリデーション
     * 8文字以上を許可
     */
    private fun validatePassword(): Boolean {
        return Pattern.compile("^.{8,}$")
            .matcher(password.value ?: "").matches()
    }

    /**
     * ログイン実行
     */
    fun login() {
        viewModelScope.launch {
            try {

                // デモ用ユーザーのEmail､パスワードの場合はデモ用リポジトリに切り替え
                if (email.value == getApplication<Application>().getString(R.string.test_email_text) &&
                    password.value == getApplication<Application>().getString(R.string.test_password_text)
                ) {
                    ServiceLocator.authRepository = DemoAuthRepository(app)
                    ServiceLocator.locationRepository = DemoLocationRepository(app)
                    ServiceLocator.musicRepository = DemoMusicRepository(app)

                    _isDemo.postValue(true)
                } else {
                    authRepository.loginJigokumimi(email.value!!, password.value!!)
                    _isLogin.postValue(true)
                }

            } catch (e: Exception) {
                handleAuthException(e)
            }
        }
    }

    /**
     * ログイン後にフラグリセット
     */
    fun doneLogin() {
        _isLogin.postValue(false)
    }

    /**
     * デモモード切り替え後にフラグリセット
     */
    fun doneDemo() {
        _isDemo.postValue(false)
    }

    /**
     * Vidwmodel破棄時にリソース開放
     */
    override fun onCleared() {
        super.onCleared()
        _loginButtonEnabledState.removeSource(email)
        _loginButtonEnabledState.removeSource(password)
    }


    /**
     * Ripositoryをリセット
     */
    fun initRepository(){
        ServiceLocator.authRepository = AuthRepository(
            PreferenceManager.getDefaultSharedPreferences(app.applicationContext),
            JigokumimiApi.retrofitService,
            SpotifyApi.retrofitService
        )
        ServiceLocator.locationRepository = LocationRepository(
            app
        )
        ServiceLocator.musicRepository = MusicRepository(
            PreferenceManager.getDefaultSharedPreferences(app.applicationContext),
            SpotifyApi.retrofitService,
            JigokumimiApi.retrofitService
        )
    }

    /**
     * Factoryクラス
     */
    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(
                    app, (app.applicationContext as JigokumimiApplication).authRepository
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}