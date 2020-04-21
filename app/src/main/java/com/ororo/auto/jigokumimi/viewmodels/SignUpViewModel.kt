package com.ororo.auto.jigokumimi.viewmodels

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.*
import com.ororo.auto.jigokumimi.JigokumimiApplication
import com.ororo.auto.jigokumimi.network.SignUpRequest
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import kotlinx.coroutines.launch
import java.util.regex.Pattern

/**
 * 新規登録画面のViewModel
 */
class SignUpViewModel(application: Application, authRepository: IAuthRepository) :
    BaseAndroidViewModel(application, authRepository) {

    /**
     *  登録状態
     */
    private var _isSignUp = MutableLiveData(false)
    val isSignUp: LiveData<Boolean>
        get() = _isSignUp

    /**
     *  ログイン状態
     */
    private var _isLogin = MutableLiveData(false)
    val isLogin: LiveData<Boolean>
        get() = _isLogin

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
     * 新規登録ボタンの活性・非活性
     */
    private val _signUpButtonEnabledState = MediatorLiveData<Boolean>()
    val signUpButtonEnabledState: LiveData<Boolean>
        get() = _signUpButtonEnabledState

    /**
     * ViewModel生成時の処理
     */
    init {
        // EditTextが変更された時にバリデーションを実行し、新規登録ボタンの活性・非活性制御､サジェストの表示･非表示を行う
        _signUpButtonEnabledState.addSource(email) { validateSignUp() }
        _signUpButtonEnabledState.addSource(password) { validateSignUp() }
        _signUpButtonEnabledState.addSource(passwordConfirmation) { validateSignUp() }
    }

    /**
     * 入力をクリアする
     */
    fun clearInput() {
        email.value = ""
        password.value = ""
        passwordConfirmation.value = ""
    }

    /**
     * 画面全体のバリデーション
     */
    private fun validateSignUp() {
        _signUpButtonEnabledState.value =
            validateEmail() && validatePassword() && validateConfirmPassword()
    }

    /**
     * Emailのバリデーション
     * ・メールアドレス形式
     */
    private fun validateEmail(): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email.value ?: "").matches()
    }

    /**
     * Passwordのバリデーション
     * ・8文字以上
     */
    private fun validatePassword(): Boolean {
        return Pattern.compile("^.{8,}$")
            .matcher(password.value ?: "").matches()
    }

    /**
     * 再入力Passwordのバリデーション
     * パスワードと入力内容が等しい
     */
    private fun validateConfirmPassword(): Boolean {
        return password.value == passwordConfirmation.value
    }

    /**
     * 新規登録実行
     */
    fun signUp() {
        viewModelScope.launch {
            try {
                authRepository.signUpJigokumimi(
                    SignUpRequest(
                        email = email.value!!,
                        password = password.value!!,
                        passwordConfirmation = passwordConfirmation.value!!
                    )
                )
                _isSignUp.value = true
            } catch (e: Exception) {
                handleAuthException(e)
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
                handleAuthException(e)
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
                return SignUpViewModel(
                    app,
                    (app.applicationContext as JigokumimiApplication).authRepository
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}
