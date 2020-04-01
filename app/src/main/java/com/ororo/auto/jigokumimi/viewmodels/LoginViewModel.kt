package com.ororo.auto.jigokumimi.viewmodels

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.*
import com.ororo.auto.jigokumimi.JigokumimiApplication
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.repository.AuthRepository
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.regex.Pattern

/**
 * ログイン画面のViewModel
 */
class LoginViewModel(application: Application, val authRepository: IAuthRepository) :
    BaseAndroidViewModel(application) {

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

    /**
     * ログインボタンの活性・非活性(Private)
     */
    private val _loginButtonEnabledState = MediatorLiveData<Boolean>()

    /**
     * ログインボタンの活性・非活性
     */
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
                        getApplication<Application>().getString(
                            R.string.general_error_message,
                            e.javaClass
                        )
                    }
                }
                showMessageDialog(msg)
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
     * Vidwmodel破棄時にリソース開放
     */
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
                return LoginViewModel(
                    app,(app.applicationContext as JigokumimiApplication).authRepository
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}