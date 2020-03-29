package com.ororo.auto.jigokumimi.viewmodels

import CoroutinesTestRule
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.repository.faker.FakeAuthRepository
import getOrAwaitValue
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.hamcrest.core.IsEqual
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.*
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.isAccessible

@RunWith(AndroidJUnit4::class)
class LoginViewModelTest {

    lateinit var viewModel: LoginViewModel

    val faker = Faker(Locale("jp_JP"))

    // LiveDataのテストに必要なルールを設定
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createViewModel() {
        viewModel = LoginViewModel(
            ApplicationProvider.getApplicationContext(),
            FakeAuthRepository()
        )
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

    @Test
    fun login_例外が発生しない_ログインフラグが立つこと() {

        // ログイン情報設定
        viewModel.email.value = faker.internet().safeEmailAddress()
        viewModel.password.value = faker.random().hex()

        // フラグをoff
        viewModel.isLogin.value = false
        runBlocking {
            // メソッド呼び出し
            viewModel.login()

        }

        val ret = viewModel.isLogin.getOrAwaitValue()
        assertThat(ret, IsEqual(true))
    }

    @Test
    fun login_HTTPExceprion発生_ログインフラグが立たずレスポンスBody内のmessageがダイアログメッセージに設定されること() {

        // エラーメッセージを追加
        val message = faker.lorem().sentence()
        // 発生させるExceptionを生成
        val exception = HttpException(
            Response.error<Any>(
                400, ResponseBody.create(
                    MediaType.parse("application/json"),
                    "{\"message\":\"$message\"}"
                )
            )
        )

        //  Repositoryに発生させる例外を指定してViewModelを生成
        viewModel = LoginViewModel(
            ApplicationProvider.getApplicationContext(),
            FakeAuthRepository(exception)
        )

        // フラグをoff
        viewModel.isLogin.value = false

        // ログイン情報設定
        viewModel.email.value = faker.internet().safeEmailAddress()
        viewModel.password.value = faker.random().hex()

        // メソッド呼び出し
        viewModel.login()

        val ret = viewModel.isLogin.getOrAwaitValue()
        assertThat(ret, IsEqual(false))

        assertThat(viewModel.errorMessage.getOrAwaitValue(), IsEqual(message))
        assertThat(viewModel.isErrorDialogShown.getOrAwaitValue(), IsEqual(true))
    }

    @Test
    fun login_IOExceprion発生_ログインフラグが立たず定型メッセージがダイアログメッセージに設定されること() {

        // 発生させるExceptionを生成
        val exception = IOException()

        //  Repositoryに発生させる例外を指定してViewModelを生成
        viewModel = LoginViewModel(
            ApplicationProvider.getApplicationContext(),
            FakeAuthRepository(exception)
        )

        // フラグをoff
        viewModel.isLogin.value = false

        // ログイン情報設定
        viewModel.email.value = faker.internet().safeEmailAddress()
        viewModel.password.value = faker.random().hex()

        // メソッド呼び出し
        viewModel.login()

        val ret = viewModel.isLogin.getOrAwaitValue()
        assertThat(ret, IsEqual(false))

        val extectedMessage =
            InstrumentationRegistry.getInstrumentation().context.resources.getString(
                R.string.no_connection_error_message
            )

        assertThat(viewModel.errorMessage.getOrAwaitValue(), IsEqual(extectedMessage))
        assertThat(viewModel.isErrorDialogShown.getOrAwaitValue(), IsEqual(true))
    }

    @Test
    fun login_Exceprion発生_ログインフラグが立たず定型メッセージがダイアログメッセージに設定されること() {

        // 発生させるExceptionを生成
        val exception = Exception()

        //  Repositoryに発生させる例外を指定してViewModelを生成
        viewModel = LoginViewModel(
            ApplicationProvider.getApplicationContext(),
            FakeAuthRepository(exception)
        )

        // フラグをoff
        viewModel.isLogin.value = false

        // ログイン情報設定
        viewModel.email.value = faker.internet().safeEmailAddress()
        viewModel.password.value = faker.random().hex()

        // メソッド呼び出し
        viewModel.login()

        val ret = viewModel.isLogin.getOrAwaitValue()
        assertThat(ret, IsEqual(false))

        val extectedMessage =
            InstrumentationRegistry.getInstrumentation().context.resources.getString(
                R.string.general_error_message, exception.javaClass
            )

        assertThat(viewModel.errorMessage.getOrAwaitValue(), IsEqual(extectedMessage))
        assertThat(viewModel.isErrorDialogShown.getOrAwaitValue(), IsEqual(true))
    }

    @Test
    fun doneLogin_isLoginがfalseになること() {
        viewModel.isLogin.value = true

        viewModel.doneLogin()

        assertThat(viewModel.isLogin.getOrAwaitValue(), IsEqual(false))

    }
}