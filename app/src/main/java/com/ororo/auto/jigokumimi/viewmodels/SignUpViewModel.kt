package com.ororo.auto.jigokumimi.viewmodels

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.network.SignUpRequest
import com.ororo.auto.jigokumimi.repository.AuthRepository
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.regex.Pattern

/**
 * 新規登録画面のViewModel
 */
class SignUpViewModel(application: Application, private val authRepository: IAuthRepository) :
    BaseAndroidViewModel(application) {

    /**
     *  登録状態(Private)
     */
    private var _isSignUp = MutableLiveData(false)

    /**
     *  登録状態
     */
    val isSignUp: LiveData<Boolean>
        get() = _isSignUp

    /**
     *  ログイン状態(Private)
     */
    private var _isLogin = MutableLiveData(false)

    /**
     *  ログイン状態
     */
    val isLogin: LiveData<Boolean>
        get() = _isLogin

    /**
     * 名前の入力内容を保持
     */
    val name = MutableLiveData("")

    /**
     * Emailの入力内容を保持
     */
    val email = MutableLiveData("")

    /**
     * Passwordの入力内容を保持
     */
    val password = MutableLiveData("")

    /**
     * 再入力Passwordの入力内容を保持
     */
    val passwordConfirmation = MutableLiveData("")

    /**
     * 新規登録ボタンの活性・非活性(Private)
     */
    private val _signUpButtonEnabledState = MediatorLiveData<Boolean>()

    /**
     * 新規登録ボタンの活性・非活性
     */
    val signUpButtonEnabledState: LiveData<Boolean>
        get() = _signUpButtonEnabledState

    init {
        // EditTextが変更された時にバリデーションを実行し、新規登録ボタンの活性・非活性制御を行う
        _signUpButtonEnabledState.addSource(name) { validateSignUp() }
        _signUpButtonEnabledState.addSource(email) { validateSignUp() }
        _signUpButtonEnabledState.addSource(password) { validateSignUp() }
        _signUpButtonEnabledState.addSource(passwordConfirmation) { validateSignUp() }
    }

    /**
     * 画面全体のバリデーション
     */
    private fun validateSignUp() {
        _signUpButtonEnabledState.value = validateEmail() && validatePassword() && validateName()
    }

    /**
     * Emailのバリデーション
     * ・メールアドレス形式
     */
    private fun validateEmail() = Patterns.EMAIL_ADDRESS.matcher(email.value ?: "").matches()

    /**
     * Passwordのバリデーション
     * ・8文字以上
     * ・再入力パスワードが等しい
     */
    private fun validatePassword(): Boolean {
        val isOkPattern = Pattern.compile("^.{8,}$")
            .matcher(password.value ?: "").matches()

        val isRetyped = password.value == passwordConfirmation.value

        return isOkPattern && isRetyped
    }

    /**
     * 名前のバリデーション
     * ・空文字以外
     */
    private fun validateName(): Boolean {
        return name.value != ""
    }

    /**
     * 新規登録実行
     */
    fun signUp() {
        viewModelScope.launch {
            try {
                authRepository.signUpJigokumimi(
                    SignUpRequest(
                        name = name.value!!,
                        email = email.value!!,
                        password = password.value!!,
                        passwordConfirmation = passwordConfirmation.value!!
                    )
                )
                _isSignUp.value = true
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
     * ログイン実行
     */
    fun login() {
        viewModelScope.launch {
            try {
                authRepository.loginJigokumimi(email.value!!, password.value!!)
                _isLogin.value = true
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
     * 新規登録後にフラグリセット
     */
    fun doneSignUp() {
        _isSignUp.postValue(false)
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
        _signUpButtonEnabledState.removeSource(name)
        _signUpButtonEnabledState.removeSource(email)
        _signUpButtonEnabledState.removeSource(password)
        _signUpButtonEnabledState.removeSource(passwordConfirmation)
    }

    /**
     * Factoryクラス
     */
    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SignUpViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SignUpViewModel(app, AuthRepository.getRepository(app)) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}
