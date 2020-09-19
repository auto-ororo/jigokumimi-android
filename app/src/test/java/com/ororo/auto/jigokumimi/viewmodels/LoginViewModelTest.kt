package com.ororo.auto.jigokumimi.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.ui.login.LoginViewModel
import getOrAwaitValue
import io.mockk.every
import io.mockk.mockk
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
class LoginViewModelTest {

    lateinit var viewModel: LoginViewModel
    lateinit var authRepository: IAuthRepository

    val faker = Faker(Locale("jp_JP"))

    // LiveDataのテストに必要なルールを設定
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createViewModel() {
        authRepository = mockk(relaxed = true)
        viewModel = LoginViewModel(
            ApplicationProvider.getApplicationContext(),
            authRepository
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
        viewModel.password.value = "12345678"

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
        viewModel.password.value = "1234567"

        // privateメソッドを取得
        val method = viewModel::class.memberFunctions.find { it.name == "validatePassword" }
        val ret = method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel) as Boolean
        }

        assertThat(ret, IsEqual(false))
    }

//    @Test
//    fun login_例外が発生しない_ログインフラグが立つこと() {
//
//        // ログイン情報設定
//        viewModel.email.value = faker.internet().safeEmailAddress()
//        viewModel.password.value = faker.random().hex()
//
//        // フラグをoff
//        viewModel.isLogin.value = false
//        runBlocking {
//            // メソッド呼び出し
//            viewModel.login()
//
//        }
//
//        val ret = viewModel.isLogin.getOrAwaitValue()
//        assertThat(ret, IsEqual(true))
//    }
//
//    @Test
//    fun login_例外発生_ログインフラグが立たずエラーメッセージが設定されること() {
//
//        // Exceptionを生成
//        val exception = Exception()
//        every { runBlocking { authRepository.loginJigokumimi(any(), any()) } } throws exception
//
//        // フラグをoff
//        viewModel.isLogin.value = false
//
//        // ログイン情報設定
//        viewModel.email.value = faker.internet().safeEmailAddress()
//        viewModel.password.value = faker.random().hex()
//
//        // メソッド呼び出し
//        viewModel.login()
//
//        val ret = viewModel.isLogin.getOrAwaitValue()
//        assertThat(ret, IsEqual(false))
//
//        val extectedMessage =
//            InstrumentationRegistry.getInstrumentation().context.resources.getString(
//                R.string.general_error_message, exception.javaClass
//            )
//
//        assertThat(viewModel.errorMessage.getOrAwaitValue(), IsEqual(extectedMessage))
//        assertThat(viewModel.isErrorDialogShown.getOrAwaitValue(), IsEqual(true))
//    }
//
//    @Test
//    fun doneLogin_isLoginがfalseになること() {
//        viewModel.isLogin.value = true
//
//        viewModel.doneLogin()
//
//        assertThat(viewModel.isLogin.getOrAwaitValue(), IsEqual(false))
//
//    }
}