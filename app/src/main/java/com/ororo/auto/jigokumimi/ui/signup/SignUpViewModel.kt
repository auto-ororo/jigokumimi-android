package com.ororo.auto.jigokumimi.ui.signup

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ororo.auto.jigokumimi.network.SignUpRequest
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.ui.common.BaseAndroidViewModel
import com.ororo.auto.jigokumimi.util.SingleLiveEvent
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
    private var _signUpFinished = SingleLiveEvent<Unit>()
    val signUpFinished: LiveData<Unit>
        get() = _signUpFinished

    /**
     *  ログイン状態
     */
    private var _loginFinished = SingleLiveEvent<Unit>()
    val loginFinished: LiveData<Unit>
        get() = _loginFinished

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

        // 処理中はタップ不可
        _signUpButtonEnabledState.addSource(isLoading) {
            _signUpButtonEnabledState.value = !it
        }
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
        _isLoading.value = true
        viewModelScope.launch {
            try {
                authRepository.signUpJigokumimi(
                    SignUpRequest(
                        email = email.value!!,
                        password = password.value!!,
                        passwordConfirmation = passwordConfirmation.value!!
                    )
                )
                _isLoading.value = false
                _signUpFinished.call()
            } catch (e: Exception) {
                handleAuthException(e)
                _isLoading.value = false
            }
        }
    }

    /**
     * ログイン実行
     */
    fun login() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                authRepository.loginJigokumimi(email.value!!, password.value!!)
                _isLoading.value = false
                _loginFinished.call()
            } catch (e: Exception) {
                handleAuthException(e)
                _isLoading.value = false
            }
        }
    }
}
