package com.ororo.auto.jigokumimi.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.network.SignUpRequest
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.ui.signup.SignUpViewModel
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
class SignUpViewModelTest {

    lateinit var viewModel: SignUpViewModel
    lateinit var authRepository: IAuthRepository

    val faker = Faker(Locale("jp_JP"))

    // LiveDataのテストに必要なルールを設定
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createViewModel() {
        authRepository = mockk(relaxed = true)

        viewModel = spyk(
            SignUpViewModel(
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
    fun validateEmail_メールアドレス形式の文字列_trueとなること() {

        // テスト用文字列を設定
        viewModel.email.value = "test@test.com"

        // privateメソッドを取得
        val method = viewModel::class.memberFunctions.find { it.name == "validateEmail" }
        val ret = method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel) as Boolean
        }

        assertThat(ret, IsEqual(true))
    }

    @Test
    fun validateEmail_アットマークが存在しない文字列_falseとなること() {

        // テスト用文字列を設定
        viewModel.email.value = "test.com"

        // privateメソッドを取得
        val method = viewModel::class.memberFunctions.find { it.name == "validateEmail" }
        val ret = method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel) as Boolean
        }

        assertThat(ret, IsEqual(false))
    }

    @Test
    fun validateEmail_ドットが存在しない文字列_がfalseとなること() {

        // テスト用文字列を設定
        viewModel.email.value = "test@com"

        // privateメソッドを取得
        val method = viewModel::class.memberFunctions.find { it.name == "validateEmail" }
        val ret = method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel) as Boolean
        }

        assertThat(ret, IsEqual(false))
    }

    @Test
    fun validateEmail_ドットよりアットマークが後に存在する文字列_falseとなること() {

        // テスト用文字列を設定
        viewModel.email.value = "te.st@com"

        // privateメソッドを取得
        val method = viewModel::class.memberFunctions.find { it.name == "validateEmail" }
        val ret = method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel) as Boolean
        }

        assertThat(ret, IsEqual(false))
    }

    @Test
    fun validatePassword_8文字以上の文字列_trueとなること() {

        // テスト用文字列を設定
        viewModel.password.value = faker.random().hex(8)

        // privateメソッドを取得
        val method = viewModel::class.memberFunctions.find { it.name == "validatePassword" }
        val ret = method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel) as Boolean
        }

        assertThat(ret, IsEqual(true))
    }

    @Test
    fun validatePassword_7文字以下の文字列_falseとなること() {

        // テスト用文字列を設定
        viewModel.password.value = faker.random().hex(7)

        // privateメソッドを取得
        val method = viewModel::class.memberFunctions.find { it.name == "validatePassword" }
        val ret = method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel) as Boolean
        }

        assertThat(ret, IsEqual(false))
    }

    @Test
    fun validateConfirmPassword_確認用パスワードの入力値が等しい_trueとなること() {

        // テスト用文字列を設定
        viewModel.password.value = faker.random().hex(8)
        viewModel.passwordConfirmation.value = viewModel.password.value

        // privateメソッドを取得
        val method = viewModel::class.memberFunctions.find { it.name == "validateConfirmPassword" }
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
        viewModel.password.value = faker.random().hex(8)
        viewModel.passwordConfirmation.value = faker.random().hex(9)

        // privateメソッドを取得
        val method = viewModel::class.memberFunctions.find { it.name == "validateConfirmPassword" }
        val ret = method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel) as Boolean
        }

        assertThat(ret, IsEqual(false))
    }

    @Test
    fun signUp__サインアップフラグがtrueになること() = runBlocking {

        val email = faker.internet().safeEmailAddress()

        val password = faker.random().hex()

        val passwordConfirmation = password
        // 登録情報設定
        viewModel.email.value = email
        viewModel.password.value = password
        viewModel.passwordConfirmation.value = passwordConfirmation

        // メソッド呼び出し
        viewModel.signUp()

        //Repositoryのメソッドが呼ばれることを確認
        verify {
            runBlocking {
                authRepository.signUpJigokumimi(
                    SignUpRequest(
                        email,
                        password,
                        passwordConfirmation
                    )
                )
            }
        }

        // サインアップフラグがtrueになること
        val ret = viewModel.isSignUp.getOrAwaitValue()
        assertThat(ret, IsEqual(true))
    }

    @Test
    fun signup_例外発生_ダイアログメッセージが設定されること() = runBlocking {

        // 例外が発生するように設定
        val exception = Exception()
        every { runBlocking { authRepository.signUpJigokumimi(any()) } } throws exception

        // 登録情報設定
        viewModel.email.value = faker.internet().safeEmailAddress()
        viewModel.password.value = faker.random().hex()
        viewModel.passwordConfirmation.value = viewModel.password.value

        // メソッド呼び出し
        viewModel.signUp()

        // エラーメッセージが設定されることを確認
        val extectedMessage =
            InstrumentationRegistry.getInstrumentation().context.resources.getString(
                R.string.general_error_message, exception.javaClass
            )
        assertThat(viewModel.errorMessage.getOrAwaitValue(), IsEqual(extectedMessage))
        assertThat(viewModel.isErrorDialogShown.getOrAwaitValue(), IsEqual(true))
    }

    @Test
    fun login_ログインフラグがTrueになること() = runBlocking {

        val email = faker.internet().safeEmailAddress()
        val password = faker.random().hex()

        // ログイン情報設定
        viewModel.email.value = email
        viewModel.password.value = password

        viewModel.doneLogin()

        viewModel.login()

        // Repositoryのメソッドが呼ばれることを確認
        verify {
            runBlocking {
                authRepository.loginJigokumimi(
                    email,
                    password
                )
            }
        }

        // ログインフラグがTrueになることを確認
        val ret = viewModel.loginFinished.getOrAwaitValue()
        assertThat(ret, IsEqual(true))
    }

    @Test
    fun login_例外発生_ダイアログメッセージが設定されること() = runBlocking {

        // 例外が発生するように設定
        val exception = Exception()
        every { runBlocking { authRepository.loginJigokumimi(any(), any()) } } throws exception

        // ログイン情報設定
        viewModel.email.value = faker.internet().safeEmailAddress()
        viewModel.password.value = faker.random().hex()

        // メソッド呼び出し
        viewModel.login()

        val ret = viewModel.loginFinished.getOrAwaitValue()
        assertThat(ret, IsEqual(false))

        val extectedMessage =
            InstrumentationRegistry.getInstrumentation().context.resources.getString(
                R.string.general_error_message, exception.javaClass
            )

        assertThat(viewModel.errorMessage.getOrAwaitValue(), IsEqual(extectedMessage))
        assertThat(viewModel.isErrorDialogShown.getOrAwaitValue(), IsEqual(true))
    }

}