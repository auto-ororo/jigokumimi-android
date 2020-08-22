package com.ororo.auto.jigokumimi.ui.setting

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ororo.auto.jigokumimi.network.ChangePasswordRequest
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.ui.common.BaseAndroidViewModel
import kotlinx.coroutines.launch
import java.util.regex.Pattern

/**
 * 設定画面のViewModel
 */
class SettingViewModel(application: Application, authRepository: IAuthRepository) :
    BaseAndroidViewModel(application, authRepository) {

    /**
     *  パスワード変更状態
     */
    private var _changedPassword = MutableLiveData(false)
    val isChangedPassword: LiveData<Boolean>
        get() = _changedPassword

    /**
     *  登録解除状態
     */
    private var _isUnregistered = MutableLiveData(false)
    val isUnregistered: LiveData<Boolean>
        get() = _isUnregistered

    /**
     * 現在のPasswordの入力内容を保持
     */
    val currentPassword = MutableLiveData("")

    /**
     * 新Passwordの入力内容を保持
     */
    val newPassword = MutableLiveData("")

    /**
     * 再入力Passwordの入力内容を保持
     */
    val newPasswordConfirmation = MutableLiveData("")

    /**
     * 新規登録ボタンの活性・非活性
     */
    private val _changePasswordButtonEnabledState = MediatorLiveData<Boolean>()
    val changePasswordButtonEnabledState: LiveData<Boolean>
        get() = _changePasswordButtonEnabledState

    /**
     * ViewModel生成時の処理
     */
    init {
        // EditTextが変更された時にバリデーションを実行し、新規登録ボタンの活性・非活性制御､サジェストの表示･非表示を行う
        _changePasswordButtonEnabledState.addSource(currentPassword) { validateSignUp() }
        _changePasswordButtonEnabledState.addSource(newPassword) { validateSignUp() }
        _changePasswordButtonEnabledState.addSource(newPasswordConfirmation) { validateSignUp() }

        // 処理中はタップ不可
        _changePasswordButtonEnabledState.addSource(isLoading) {
            _changePasswordButtonEnabledState.value = !it
        }
    }

    /**
     * 入力をクリアする
     */
    fun clearInput() {
        currentPassword.value = ""
        newPassword.value = ""
        newPasswordConfirmation.value = ""
    }

    /**
     * 画面全体のバリデーション
     */
    private fun validateSignUp() {
        _changePasswordButtonEnabledState.value =
            validateCurrentPassword() && validateNewPassword() && validateNewConfirmPassword()
    }

    /**
     * 現在のPasswordのバリデーション
     * ・8文字以上
     */
    private fun validateCurrentPassword(): Boolean {
        return Pattern.compile("^.{8,}$")
            .matcher(currentPassword.value ?: "").matches()
    }

    /**
     * 新Passwordのバリデーション
     * ・8文字以上
     */
    private fun validateNewPassword(): Boolean {
        return Pattern.compile("^.{8,}$")
            .matcher(newPassword.value ?: "").matches()
    }

    /**
     * 再入力Passwordのバリデーション
     * 新パスワードと入力内容が等しい
     */
    private fun validateNewConfirmPassword(): Boolean {
        return newPassword.value == newPasswordConfirmation.value
    }

    /**
     * パスワード変更実行
     */
    fun changePassword() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                authRepository.changeJigokumimiPassword(
                    ChangePasswordRequest(
                        currentPassword = currentPassword.value!!,
                        newPassword = newPassword.value!!,
                        newPasswordConfirmation = newPasswordConfirmation.value!!
                    )
                )
                _isLoading.value = false
                _changedPassword.value = true
            } catch (e: Exception) {
                handleAuthException(e)
                _isLoading.value = false
            }
        }
    }

    /**
     * 登録解除実行
     */
    fun unregister() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                authRepository.unregisterJigokumimiUser()
                _isLoading.value = false
                _isUnregistered.value = true
            } catch (e: Exception) {
                handleAuthException(e)
                _isLoading.value = false
            }
        }
    }

    /**
     * パスワード変更後にフラグリセット
     */
    fun doneChangePassword() {
        _changedPassword.postValue(false)
    }

    /**
     * 登録解除後にフラグリセット
     */
    fun doneUnregister() {
        _isUnregistered.postValue(false)
    }
}
