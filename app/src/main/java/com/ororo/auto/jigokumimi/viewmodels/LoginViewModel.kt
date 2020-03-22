package com.ororo.auto.jigokumimi.viewmodels

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.network.JigokumimiApi
import com.ororo.auto.jigokumimi.network.JigokumimiApiService
import com.ororo.auto.jigokumimi.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.util.regex.Pattern

class LoginViewModel(application: Application) : BaseAndroidViewModel(application) {

    /**
     * 認証系リポジトリ
     */
    val authRepository = AuthRepository(
        PreferenceManager.getDefaultSharedPreferences(application.applicationContext)
    )

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
     * Emailの入力内容を保持
     */
    val email = MutableLiveData<String>()

    /**
     * Passwordの入力内容を保持
     */
    val password = MutableLiveData<String>()

    private val _loginButtonEnabledState = MediatorLiveData<Boolean>()

    val loginButtonEnabledState: LiveData<Boolean>
        get() = _loginButtonEnabledState

    init {
        // Email,Passwordが変更された時にバリデーションを実行し、ログインボタンの活性・非活性制御を行う
        _loginButtonEnabledState.addSource(email) { validateLogin() }
        _loginButtonEnabledState.addSource(password) { validateLogin() }
    }

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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                authRepository.loginJigokumimi(email.value!!, password.value!!)
                _isLogin.postValue(true)
            } catch (e: Exception) {
                val msg = when (e) {
                    is HttpException -> {
                        getMessageFromHttpException(e)
                    }
                    is IOException -> {
                        getApplication<Application>().getString(R.string.no_connection_error_message)
                    }
                    else -> {
                        getApplication<Application>().getString(R.string.general_error_message, e.javaClass)
                    }
                }
                showMessageDialog(msg)
            }
        }
    }

    // Vidwmodel破棄時にリソース開放
    override fun onCleared() {
        super.onCleared()
        _loginButtonEnabledState.removeSource(email)
        _loginButtonEnabledState.removeSource(password)
    }

    /**
     * Factoryクラス
     */
    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}