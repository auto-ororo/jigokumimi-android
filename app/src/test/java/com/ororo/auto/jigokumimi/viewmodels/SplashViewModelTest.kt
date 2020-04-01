package com.ororo.auto.jigokumimi.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import getOrAwaitValue
import io.mockk.every
import io.mockk.mockk
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
import java.util.*

@RunWith(AndroidJUnit4::class)
class SplashViewModelTest {

    lateinit var viewModel: SplashViewModel

    val faker = Faker(Locale("jp_JP"))

    val mockAuthRepo = mockk<IAuthRepository>(relaxed = true)

    // LiveDataのテストに必要なルールを設定
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createViewModel() {
        viewModel = SplashViewModel(
            ApplicationProvider.getApplicationContext(),
            mockAuthRepo
        )
    }

    @Test
    fun loginBySavedInput_emailとpasswordが空文字以外_ログインフラグと初期化状態フラグがTrueになること() {


        // emailとpasswordを空文字以外に設定
        val sampleEmail = faker.internet().safeEmailAddress()
        val samplePassword = faker.random().hex()

        every {
            mockAuthRepo.getSavedLoginInfo()
        } returns Pair(sampleEmail, samplePassword)

        // メソッド呼び出し
        viewModel.loginBySavedInput()

        assertThat(viewModel.isLogin.getOrAwaitValue(), IsEqual(true))
        assertThat(viewModel.isReady.getOrAwaitValue(), IsEqual(true))
    }

    @Test
    fun loginBySavedInput_emailが空文字以外passwordが空文字_ログインフラグがFalse初期化状態フラグがTrueになること() {

        // emailとpasswordを空文字以外に設定
        val sampleEmail = faker.internet().safeEmailAddress()
        val samplePassword = ""
        every {
            mockAuthRepo.getSavedLoginInfo()
        } returns Pair(sampleEmail, samplePassword)

        // メソッド呼び出し
        viewModel.loginBySavedInput()

        assertThat(viewModel.isLogin.getOrAwaitValue(), IsEqual(false))
        assertThat(viewModel.isReady.getOrAwaitValue(), IsEqual(true))
    }

    @Test
    fun loginBySavedInput_emailが空文字passwordが空文字以外_ログインフラグがFalse初期化状態フラグがTrueになること() {
        // emailとpasswordを空文字以外に設定
        val sampleEmail = ""
        val samplePassword = faker.random().hex()
        every {
            mockAuthRepo.getSavedLoginInfo()
        } returns Pair(sampleEmail, samplePassword)

        // メソッド呼び出し
        viewModel.loginBySavedInput()

        assertThat(viewModel.isLogin.getOrAwaitValue(), IsEqual(false))
        assertThat(viewModel.isReady.getOrAwaitValue(), IsEqual(true))

    }

    @Test
    fun loginBySavedInput_emailとpasswordが空文字_ログインフラグがfalse初期化状態フラグがtrueになること() {

        // emailとpasswordを空文字以外に設定
        val sampleEmail = ""
        val samplePassword = ""
        every {
            mockAuthRepo.getSavedLoginInfo()
        } returns Pair(sampleEmail, samplePassword)

        // メソッド呼び出し
        viewModel.loginBySavedInput()

        assertThat(viewModel.isLogin.getOrAwaitValue(), IsEqual(false))
        assertThat(viewModel.isReady.getOrAwaitValue(), IsEqual(true))
    }

    @Test
    fun loginBySavedInput_Exception_ログインフラグがfalse初期化状態フラグがtrueになること() {

        // 発生させるExceptionを生成
        val exception = HttpException(
            Response.error<Any>(
                400, ResponseBody.create(
                    MediaType.parse("application/json"),
                    "{}"
                )
            )
        )
        every { mockAuthRepo.getSavedLoginInfo() } throws exception

        // メソッド呼び出し
        viewModel.loginBySavedInput()

        assertThat(viewModel.isLogin.getOrAwaitValue(), IsEqual(false))
        assertThat(viewModel.isReady.getOrAwaitValue(), IsEqual(true))
    }

}