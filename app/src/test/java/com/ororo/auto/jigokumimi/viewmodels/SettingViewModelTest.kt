package com.ororo.auto.jigokumimi.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.network.ChangePasswordRequest
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.ui.setting.SettingViewModel
import getOrAwaitValue
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.hamcrest.core.IsEqual
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import java.util.*
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.isAccessible


@RunWith(AndroidJUnit4::class)
class SettingViewModelTest {

    lateinit var viewModel: SettingViewModel
    lateinit var authRepository: IAuthRepository

    val faker = Faker(Locale("jp_JP"))

    // LiveDataのテストに必要なルールを設定
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createViewModel() {
        authRepository = mockk(relaxed = true)

        viewModel = spyk(
            SettingViewModel(
                ApplicationProvider.getApplicationContext(),
                authRepository
            )
        )
    }

    @After
    fun shutDownWebServer() {
        stopKoin()
    }

    @Test
    fun validateCurrentPassword_8文字以上の文字列_trueとなること() {

        // テスト用文字列を設定
        viewModel.currentPassword.value = faker.random().hex(8)

        // privateメソッドを取得
        val method = viewModel::class.memberFunctions.find { it.name == "validateCurrentPassword" }
        val ret = method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel) as Boolean
        }

        assertThat(ret, IsEqual(true))
    }

    @Test
    fun validateCurrentPassword_7文字以下の文字列_falseとなること() {

        // テスト用文字列を設定
        viewModel.currentPassword.value = faker.random().hex(7)

        // privateメソッドを取得
        val method = viewModel::class.memberFunctions.find { it.name == "validateCurrentPassword" }
        val ret = method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel) as Boolean
        }

        assertThat(ret, IsEqual(false))
    }

    @Test
    fun validateNewPassword_8文字以上の文字列_trueとなること() {

        // テスト用文字列を設定
        viewModel.newPassword.value = faker.random().hex(8)

        // privateメソッドを取得
        val method = viewModel::class.memberFunctions.find { it.name == "validateNewPassword" }
        val ret = method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel) as Boolean
        }

        assertThat(ret, IsEqual(true))
    }

    @Test
    fun validateNewPassword_7文字以下の文字列_falseとなること() {

        // テスト用文字列を設定
        viewModel.newPassword.value = faker.random().hex(7)

        // privateメソッドを取得
        val method = viewModel::class.memberFunctions.find { it.name == "validateNewPassword" }
        val ret = method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel) as Boolean
        }

        assertThat(ret, IsEqual(false))
    }

    @Test
    fun validateNewConfirmPassword_確認用パスワードの入力値が等しい_trueとなること() {

        // テスト用文字列を設定
        viewModel.newPassword.value = faker.random().hex(8)
        viewModel.newPasswordConfirmation.value = viewModel.newPassword.value

        // privateメソッドを取得
        val method =
            viewModel::class.memberFunctions.find { it.name == "validateNewConfirmPassword" }
        val ret = method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel) as Boolean
        }

        assertThat(ret, IsEqual(true))
    }

    @Test
    fun validateConfirmPassword_確認用パスワードの入力値が異なる_falseとなること() {

        // テスト用文字列を設定
        viewModel.newPassword.value = faker.random().hex(8)
        viewModel.newPasswordConfirmation.value = faker.random().hex(9)

        // privateメソッドを取得
        val method =
            viewModel::class.memberFunctions.find { it.name == "validateNewConfirmPassword" }
        val ret = method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel) as Boolean
        }

        assertThat(ret, IsEqual(false))
    }


    @Test
    fun clearInput_パスワード入力値が初期化されること() {

        // テスト用文字列を設定
        viewModel.currentPassword.value = faker.random().hex(8)
        viewModel.newPassword.value = faker.random().hex(8)
        viewModel.newPasswordConfirmation.value = faker.random().hex(9)

        viewModel.clearInput()

        assertThat(viewModel.currentPassword.getOrAwaitValue(), IsEqual(""))
        assertThat(viewModel.newPassword.getOrAwaitValue(), IsEqual(""))
        assertThat(viewModel.newPasswordConfirmation.getOrAwaitValue(), IsEqual(""))
    }

    @Test
    fun changePassword_パスワード変更フラグがtrueになること() = runBlocking {

        // フラグリセット
        viewModel.doneChangePassword()

        val currentPassword = faker.random().hex()
        val newPassword = faker.random().hex()
        val newPasswordConfirmation = newPassword

        // 登録情報設定
        viewModel.currentPassword.value = currentPassword
        viewModel.newPassword.value = newPassword
        viewModel.newPasswordConfirmation.value = newPasswordConfirmation

        // メソッド呼び出し
        viewModel.changePassword()

        //Repositoryのメソッドが呼ばれることを確認
        verify {
            runBlocking {
                authRepository.changeJigokumimiPassword(
                    ChangePasswordRequest(
                        currentPassword,
                        newPassword,
                        newPasswordConfirmation
                    )
                )
            }
        }

        // パウワード変更フラグがtrueになること
        val ret = viewModel.changedPassword.getOrAwaitValue()
        assertThat(ret, IsEqual(true))
    }

    @Test
    fun signup_例外発生_ダイアログメッセージが設定されること() = runBlocking {

        // 例外が発生するように設定
        val exception = Exception()
        every { runBlocking { authRepository.changeJigokumimiPassword(any()) } } throws exception

        val currentPassword = faker.random().hex()
        val newPassword = faker.random().hex()
        val newPasswordConfirmation = newPassword

        // 登録情報設定
        viewModel.currentPassword.value = currentPassword
        viewModel.newPassword.value = newPassword
        viewModel.newPasswordConfirmation.value = newPasswordConfirmation

        // メソッド呼び出し
        viewModel.changePassword()

        // エラーメッセージが設定されることを確認
        val extectedMessage =
            InstrumentationRegistry.getInstrumentation().context.resources.getString(
                R.string.general_error_message, exception.javaClass
            )
        assertThat(viewModel.errorMessage.getOrAwaitValue(), IsEqual(extectedMessage))
        assertThat(viewModel.isErrorDialogShown.getOrAwaitValue(), IsEqual(true))
    }

    @Test
    fun unregister_登録解除フラグがTrueになること() = runBlocking {

        // フラグリセット
        viewModel.doneUnregister()

        viewModel.unregister()

        // Repositoryのメソッドが呼ばれることを確認
        verify {
            runBlocking {
                authRepository.unregisterJigokumimiUser()
            }
        }

        // 登録解除フラグがTrueになることを確認
        val ret = viewModel.unregistered.getOrAwaitValue()
        assertThat(ret, IsEqual(true))
    }

    @Test
    fun unregister_例外発生_ダイアログメッセージが設定されること() = runBlocking {

        // 例外が発生するように設定
        val exception = Exception()
        every { runBlocking { authRepository.unregisterJigokumimiUser() } } throws exception

        // メソッド呼び出し
        viewModel.unregister()

        // エラーメッセージが設定されることを確認
        val extectedMessage =
            InstrumentationRegistry.getInstrumentation().context.resources.getString(
                R.string.general_error_message, exception.javaClass
            )
        assertThat(viewModel.errorMessage.getOrAwaitValue(), IsEqual(extectedMessage))
        assertThat(viewModel.isErrorDialogShown.getOrAwaitValue(), IsEqual(true))
    }
}