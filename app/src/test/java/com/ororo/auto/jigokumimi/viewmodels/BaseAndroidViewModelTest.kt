package com.ororo.auto.jigokumimi.viewmodels

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.R
import getOrAwaitValue
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.hamcrest.core.IsEqual
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.HttpException
import retrofit2.Response
import java.util.*
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.isAccessible

@RunWith(AndroidJUnit4::class)
class BaseAndroidViewModelTest {

    lateinit var viewModel: BaseAndroidViewModel

    val faker = Faker(Locale("jp_JP"))

    @Before
    fun createViewModel() {
        viewModel = BaseAndroidViewModel(ApplicationProvider.getApplicationContext())

    }

    @Test
    fun moveLoginDone_isTokenExpiredがFalseであること() {
        viewModel.moveLoginDone()
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
            InstrumentationRegistry.getInstrumentation().context.resources.getString(R.string.request_fail_message)

        // 汎用メッセージが取得できることを確認
        assertThat(message, IsEqual(retMessage))
    }


}