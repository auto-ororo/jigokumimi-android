package com.ororo.auto.jigokumimi.viewmodels

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.ui.common.BaseAndroidViewModel
import getOrAwaitValue
import io.mockk.mockk
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.hamcrest.core.IsEqual
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.*
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.isAccessible

@RunWith(AndroidJUnit4::class)
class BaseAndroidViewModelTest {

    lateinit var viewModel: BaseAndroidViewModel
    lateinit var authRepository: IAuthRepository

    val faker = Faker(Locale("jp_JP"))

    @Before
    fun createViewModel() {
        authRepository = mockk(relaxed = true)
        viewModel =
            BaseAndroidViewModel(
                ApplicationProvider.getApplicationContext(),
                authRepository
            )

    }

    @After
    fun shutDownWebServer() {
        stopKoin()
    }

    @Test
    fun moveLoginDone_isTokenExpiredがFalseであること() {
        viewModel.onMovedLogin()
        assertThat(viewModel.isTokenExpired.getOrAwaitValue(), IsEqual(false))
    }

    @Test
    fun showMessageDialog_isErrorDialogShownがtrueとなりダイアログメッセージが設定されること() {

        val beforeDialogShownLiveDataValue = viewModel.isErrorDialogShown.getOrAwaitValue()
        assertThat(beforeDialogShownLiveDataValue, IsEqual(false))

        // 引数に渡すメッセージ
        val message = faker.lorem().sentence()
        // privateメソッドを取得
        val method = viewModel::class.memberFunctions.find { it.name == "showMessageDialog" }
        method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel, message)
        }

        val messageLiveDataValue = viewModel.errorMessage.getOrAwaitValue()
        val afterDialogShownLiveDataValue = viewModel.isErrorDialogShown.getOrAwaitValue()

        // 引数で渡したメッセージが設定されていることを確認
        assertThat(message, IsEqual(messageLiveDataValue))
        // フラグがtrueであることを確認
        assertThat(afterDialogShownLiveDataValue, IsEqual(true))
    }

    @Test
    fun showSnackBar_Snackbarメッセージが設定されること() {

        // 引数に渡すメッセージ
        val message = faker.lorem().sentence()

        // メソッド呼び出し
        viewModel.showSnackbar(message)

        // 引数で渡したメッセージが設定されていることを確認
        val messageLiveDataValue = viewModel.snackbarMessage.getOrAwaitValue()
        assertThat(message, IsEqual(messageLiveDataValue))
    }

    @Test
    fun showedSnackBar_Snackbarメッセージが初期化されること() {

        // SnackBarメッセージを設定
        val message = faker.lorem().sentence()
        viewModel.showSnackbar(message)

        // メソッド呼び出し
        viewModel.showedSnackbar()

        val messageLiveDataValue = viewModel.snackbarMessage.getOrAwaitValue()

        // 引数で渡したメッセージが設定されていることを確認
        assertThat(messageLiveDataValue, IsEqual(""))
    }

    @Test
    fun getMessageFromHttpException_messageが存在する場合にmessageを取得できること() {

        val errorMessage = faker.lorem().sentence()

        //「message」を持つレスポンスを作成
        val response = Response.error<Any>(
            400, ResponseBody.create(
                MediaType.parse("application/json"),
                "{\"message\":\"$errorMessage\"}"
            )
        )

        // privateメソッドを取得
        val method =
            viewModel::class.memberFunctions.find { it.name == "getMessageFromHttpException" }
        // 戻り値(メッセージ)を取得
        val retMessage: String? = method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel, HttpException(response)) as String
        }

        // メッセージが取得できることを確認
        assertThat(errorMessage, IsEqual(retMessage))
    }

    @Test
    fun getMessageFromHttpException_messageが存在しない場合に汎用メッセージを取得できること() {

//        「message」を持たないレスポンスを作成
        val response = Response.error<Any>(
            400, ResponseBody.create(
                MediaType.parse("application/json"),
                "{\"hogehoge\":\"nomessage\"}"
            )
        )

        // privateメソッドを取得
        val method =
            viewModel::class.memberFunctions.find { it.name == "getMessageFromHttpException" }
        // 戻り値(メッセージ)を取得
        val retMessage: String? = method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel, HttpException(response)) as String
        }

        val message =
            InstrumentationRegistry.getInstrumentation()
                .context.resources.getString(R.string.request_fail_message)

        // 汎用メッセージが取得できることを確認
        assertThat(message, IsEqual(retMessage))
    }

    @Test
    fun handleConnectException_レスポンスコード401のHttpException_トークン認証切れのメッセージが表示されること() {

        val errorMessage = faker.lorem().sentence()

        // ステータスコード401を持つレスポンスを作成
        val response = Response.error<Any>(
            401, ResponseBody.create(
                MediaType.parse("application/json"),
                "{\"message\":\"$errorMessage\"}"
            )
        )

        // privateメソッドを取得
        val method =
            viewModel::class.memberFunctions.find { it.name == "handleConnectException" }
        // メソッド実行
        method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel, HttpException(response))
        }

        // トークン認証切れメッセージが設定されていることを確認
        val messageLiveDataValue = viewModel.errorMessage.getOrAwaitValue()
        val expectedMessage =
            InstrumentationRegistry.getInstrumentation().context.resources.getString(
                R.string.token_expired_error_message
            )
        assertThat(messageLiveDataValue, IsEqual(expectedMessage))

        // メッセージ表示フラグがtrueであることを確認
        val afterDialogShownLiveDataValue = viewModel.isErrorDialogShown.getOrAwaitValue()
        assertThat(afterDialogShownLiveDataValue, IsEqual(true))
    }


    @Test
    fun handleConnectException_レスポンスコード401以外のHttpException_レスポンスBody内のmessageがエラーメッセージとして表示されること() {

        val errorMessage = faker.lorem().sentence()

        // ステータスコード400を持つレスポンスを作成
        val response = Response.error<Any>(
            400, ResponseBody.create(
                MediaType.parse("application/json"),
                "{\"message\":\"$errorMessage\"}"
            )
        )

        // privateメソッドを取得
        val method =
            viewModel::class.memberFunctions.find { it.name == "handleConnectException" }
        // メソッド実行
        method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel, HttpException(response))
        }

        // レスポンスBody内のmessageが設定されていることを確認
        val messageLiveDataValue = viewModel.errorMessage.getOrAwaitValue()
        assertThat(messageLiveDataValue, IsEqual(errorMessage))

        // メッセージ表示フラグがtrueであることを確認
        val afterDialogShownLiveDataValue = viewModel.isErrorDialogShown.getOrAwaitValue()
        assertThat(afterDialogShownLiveDataValue, IsEqual(true))
    }

    @Test
    fun handleConnectException_IOException_通信エラーが表示されること() {

        // IOExceptionを生成
        val exception = IOException()

        // privateメソッドを取得
        val method =
            viewModel::class.memberFunctions.find { it.name == "handleConnectException" }
        // メソッド実行
        method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel, exception)
        }

        // 通信エラーメッセージが設定されていることを確認
        val messageLiveDataValue = viewModel.errorMessage.getOrAwaitValue()
        val expectedMessage =
            InstrumentationRegistry.getInstrumentation().context.resources.getString(
                R.string.no_connection_error_message
            )
        assertThat(messageLiveDataValue, IsEqual(expectedMessage))

        // メッセージ表示フラグがtrueであることを確認
        val afterDialogShownLiveDataValue = viewModel.isErrorDialogShown.getOrAwaitValue()
        assertThat(afterDialogShownLiveDataValue, IsEqual(true))
    }

    @Test
    fun handleConnectException_Exception_汎用エラーが表示されること() {

        // Exceptionを生成
        val exception = Exception()

        // privateメソッドを取得
        val method =
            viewModel::class.memberFunctions.find { it.name == "handleConnectException" }
        // メソッド実行
        method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel, exception)
        }

        // 通信エラーメッセージが設定されていることを確認
        val messageLiveDataValue = viewModel.errorMessage.getOrAwaitValue()
        val expectedMessage =
            InstrumentationRegistry.getInstrumentation().context.resources.getString(
                R.string.general_error_message, exception.javaClass
            )
        assertThat(messageLiveDataValue, IsEqual(expectedMessage))

        // メッセージ表示フラグがtrueであることを確認
        val afterDialogShownLiveDataValue = viewModel.isErrorDialogShown.getOrAwaitValue()
        assertThat(afterDialogShownLiveDataValue, IsEqual(true))
    }

    @Test
    fun handleAuthException_HttpException_レスポンスBody内のmessageがエラーメッセージとして表示されること() {

        val errorMessage = faker.lorem().sentence()

        // ステータスコード400を持つレスポンスを作成
        val response = Response.error<Any>(
            400, ResponseBody.create(
                MediaType.parse("application/json"),
                "{\"message\":\"$errorMessage\"}"
            )
        )

        // privateメソッドを取得
        val method =
            viewModel::class.memberFunctions.find { it.name == "handleAuthException" }
        // メソッド実行
        method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel, HttpException(response))
        }

        // レスポンスBody内のmessageが設定されていることを確認
        val messageLiveDataValue = viewModel.errorMessage.getOrAwaitValue()
        assertThat(messageLiveDataValue, IsEqual(errorMessage))

        // メッセージ表示フラグがtrueであることを確認
        val afterDialogShownLiveDataValue = viewModel.isErrorDialogShown.getOrAwaitValue()
        assertThat(afterDialogShownLiveDataValue, IsEqual(true))
    }

    @Test
    fun handleAuthException_IOException_通信エラーが表示されること() {

        // IOExceptionを生成
        val exception = IOException()

        // privateメソッドを取得
        val method =
            viewModel::class.memberFunctions.find { it.name == "handleAuthException" }
        // メソッド実行
        method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel, exception)
        }

        // 通信エラーメッセージが設定されていることを確認
        val messageLiveDataValue = viewModel.errorMessage.getOrAwaitValue()
        val expectedMessage =
            InstrumentationRegistry.getInstrumentation().context.resources.getString(
                R.string.no_connection_error_message
            )
        assertThat(messageLiveDataValue, IsEqual(expectedMessage))

        // メッセージ表示フラグがtrueであることを確認
        val afterDialogShownLiveDataValue = viewModel.isErrorDialogShown.getOrAwaitValue()
        assertThat(afterDialogShownLiveDataValue, IsEqual(true))
    }

    @Test
    fun handleAuthException_Exception_汎用エラーが表示されること() {

        // Exceptionを生成
        val exception = Exception()

        // privateメソッドを取得
        val method =
            viewModel::class.memberFunctions.find { it.name == "handleAuthException" }
        // メソッド実行
        method?.let {
            it.isAccessible = true
            // メソッド呼び出し
            it.call(viewModel, exception)
        }

        // 通信エラーメッセージが設定されていることを確認
        val messageLiveDataValue = viewModel.errorMessage.getOrAwaitValue()
        val expectedMessage =
            InstrumentationRegistry.getInstrumentation().context.resources.getString(
                R.string.general_error_message, exception.javaClass
            )
        assertThat(messageLiveDataValue, IsEqual(expectedMessage))

        // メッセージ表示フラグがtrueであることを確認
        val afterDialogShownLiveDataValue = viewModel.isErrorDialogShown.getOrAwaitValue()
        assertThat(afterDialogShownLiveDataValue, IsEqual(true))
    }
}