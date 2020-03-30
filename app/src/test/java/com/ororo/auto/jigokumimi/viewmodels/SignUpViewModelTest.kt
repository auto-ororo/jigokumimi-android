package com.ororo.auto.jigokumimi.viewmodels

import TestCoroutineRule
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
class SignUpViewModelTest {

    lateinit var viewModel: SignUpViewModel

    val faker = Faker(Locale("jp_JP"))

    // LiveDataのテストに必要なルールを設定
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createViewModel() {
        viewModel = SignUpViewModel(
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
        viewModel.password.value = faker.random().hex(8)
        viewModel.passwordConfirmation.value = viewModel.password.value

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
        viewModel.passwordConfirmation.value = viewModel.password.value

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
    fun validatePassword_確認用パスワードの入力値が異なる_falseとなること() {

        // テスト用文字列を設定
        viewModel.password.value = faker.random().hex(8)
        viewModel.passwordConfirmation.value = faker.random().hex(9)

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
    fun validateName_空文字_falseとなること() {

        // テスト用文字列を設定
        viewModel.name.value = ""

        // privateメソッドを取得
        val method = viewModel::class.memberFunctions.find { it.name == "validateName" }
        val ret = method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel) as Boolean
        }

        assertThat(ret, IsEqual(false))
    }

    @Test
    fun validateName_空文字以外_trueとなること() {

        // テスト用文字列を設定
        viewModel.name.value = faker.name().fullName()

        // privateメソッドを取得
        val method = viewModel::class.memberFunctions.find { it.name == "validateName" }
        val ret = method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel) as Boolean
        }

        assertThat(ret, IsEqual(true))
    }

    @Test
    fun login_例外が発生しない_ログインフラグが立つこと() = runBlocking {

        // ログイン情報設定
        viewModel.email.value = faker.internet().safeEmailAddress()
        viewModel.password.value = faker.random().hex()

        viewModel.doneLogin()

        viewModel.login()

        val ret = viewModel.isLogin.getOrAwaitValue()
        assertThat(ret, IsEqual(true))
    }

    @Test
    fun login_HTTPExceprion発生_ログインフラグが立たずレスポンスBody内のmessageがダイアログメッセージに設定されること() = runBlocking {

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
        viewModel = SignUpViewModel(
            ApplicationProvider.getApplicationContext(),
            FakeAuthRepository(exception)
        )

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
    fun login_IOExceprion発生_ログインフラグが立たず定型メッセージがダイアログメッセージに設定されること() = runBlocking {

        // 発生させるExceptionを生成
        val exception = IOException()

        //  Repositoryに発生させる例外を指定してViewModelを生成
        viewModel = SignUpViewModel(
            ApplicationProvider.getApplicationContext(),
            FakeAuthRepository(exception)
        )

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
    fun login_Exceprion発生_ログインフラグが立たず定型メッセージがダイアログメッセージに設定されること() = runBlocking {

        // 発生させるExceptionを生成
        val exception = Exception()

        //  Repositoryに発生させる例外を指定してViewModelを生成
        viewModel = SignUpViewModel(
            ApplicationProvider.getApplicationContext(),
            FakeAuthRepository(exception)
        )


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
    fun signUp_例外が発生しない_サインアップフラグがtrueになること() = runBlocking {

        // 登録情報設定
        viewModel.email.value = faker.internet().safeEmailAddress()
        viewModel.password.value = faker.random().hex()
        viewModel.passwordConfirmation.value = viewModel.password.value
        viewModel.name.value = faker.name().fullName()

        // メソッド呼び出し
        viewModel.signUp()

        val ret = viewModel.isSignUp.getOrAwaitValue()
        assertThat(ret, IsEqual(true))
    }

    @Test
    fun signup_HTTPExceprion発生_ログインフラグがfalseとなりレスポンスBody内のmessageがダイアログメッセージに設定されること() =
        runBlocking {

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
            viewModel = SignUpViewModel(
                ApplicationProvider.getApplicationContext(),
                FakeAuthRepository(exception)
            )

            // 登録情報設定
            viewModel.email.value = faker.internet().safeEmailAddress()
            viewModel.password.value = faker.random().hex()
            viewModel.passwordConfirmation.value = viewModel.password.value
            viewModel.name.value = faker.random().hex()

            // メソッド呼び出し
            viewModel.signUp()

            val ret = viewModel.isSignUp.getOrAwaitValue()
            assertThat(ret, IsEqual(false))

            assertThat(viewModel.errorMessage.getOrAwaitValue(), IsEqual(message))
            assertThat(viewModel.isErrorDialogShown.getOrAwaitValue(), IsEqual(true))
        }

    @Test
    fun signup_IOExceprion発生_サインアップフラグがfalseとなり定型メッセージがダイアログメッセージに設定されること() = runBlocking {

        // 発生させるExceptionを生成
        val exception = IOException()

        //  Repositoryに発生させる例外を指定してViewModelを生成
        viewModel = SignUpViewModel(
            ApplicationProvider.getApplicationContext(),
            FakeAuthRepository(exception)
        )

        // 登録情報設定
        viewModel.email.value = faker.internet().safeEmailAddress()
        viewModel.password.value = faker.random().hex()
        viewModel.passwordConfirmation.value = viewModel.password.value
        viewModel.name.value = faker.random().hex()

        // メソッド呼び出し
        viewModel.signUp()

        val ret = viewModel.isSignUp.getOrAwaitValue()
        assertThat(ret, IsEqual(false))

        val extectedMessage =
            InstrumentationRegistry.getInstrumentation().context.resources.getString(
                R.string.no_connection_error_message
            )

        assertThat(viewModel.errorMessage.getOrAwaitValue(), IsEqual(extectedMessage))
        assertThat(viewModel.isErrorDialogShown.getOrAwaitValue(), IsEqual(true))
    }

    @Test
    fun signup_Exceprion発生_サインアップフラグがoffになり定型メッセージがダイアログメッセージに設定されること() = runBlocking {

        // 発生させるExceptionを生成
        val exception = Exception()

        //  Repositoryに発生させる例外を指定してViewModelを生成
        viewModel = SignUpViewModel(
            ApplicationProvider.getApplicationContext(),
            FakeAuthRepository(exception)
        )

        // 登録情報設定
        viewModel.email.value = faker.internet().safeEmailAddress()
        viewModel.password.value = faker.random().hex()
        viewModel.passwordConfirmation.value = viewModel.password.value
        viewModel.name.value = faker.random().hex()

        // メソッド呼び出し
        viewModel.signUp()

        val ret = viewModel.isSignUp.getOrAwaitValue()
        assertThat(ret, IsEqual(false))

        val extectedMessage =
            InstrumentationRegistry.getInstrumentation().context.resources.getString(
                R.string.general_error_message, exception.javaClass
            )

        assertThat(viewModel.errorMessage.getOrAwaitValue(), IsEqual(extectedMessage))
        assertThat(viewModel.isErrorDialogShown.getOrAwaitValue(), IsEqual(true))
    }
}