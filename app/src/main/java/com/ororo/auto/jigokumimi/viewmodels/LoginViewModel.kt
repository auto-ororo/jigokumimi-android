package com.ororo.auto.jigokumimi.viewmodels

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.util.demoRepositoryModule
import com.ororo.auto.jigokumimi.util.repositoryModule
import kotlinx.coroutines.launch
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
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
        // 処理中状態を監視
        _loginButtonEnabledState.addSource(isLoading) { _loginButtonEnabledState.value = !it }
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
        _isLoading.value = true
        viewModelScope.launch {
            try {

                // デモ用ユーザーのEmail､パスワードの場合はデモ用リポジトリに切り替え
                if (email.value == getApplication<Application>().getString(R.string.test_email_text) &&
                    password.value == getApplication<Application>().getString(R.string.test_password_text)
                ) {
                    unloadKoinModules(repositoryModule)
                    loadKoinModules(demoRepositoryModule)

                    _isDemo.postValue(true)
                } else {
                    authRepository.loginJigokumimi(email.value!!, password.value!!)
                    _isLogin.postValue(true)
                }

            } catch (e: Exception) {
                handleAuthException(e)
            }
            _isLoading.value = false
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
     * Ripositoryをリセット
     */
    fun initRepository() {
        unloadKoinModules(demoRepositoryModule)
        unloadKoinModules(repositoryModule)
        loadKoinModules(repositoryModule)
    }
}