package com.ororo.auto.jigokumimi.ui

import ServiceLocator
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import io.mockk.every
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.HttpException
import retrofit2.Response
import io.mockk.mockk
import io.mockk.verify

@MediumTest
@RunWith(AndroidJUnit4::class)
class LoginFragmentTest {

    private lateinit var repository: IAuthRepository
    private lateinit var navController: NavController

    @Before
    fun init() {
        // mock化したrepositoryをServiceLocatorへ登録し、テスト実行時に参照するように設定)
        repository = mockk(relaxed = true)
        ServiceLocator.authRepository = repository

        // 検索画面を起動し、NavControllerを設定
        val scenario = launchFragmentInContainer<LoginFragment>(null, R.style.AppTheme)
        navController = mockk<NavController>(relaxed = true)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

    }

    @Test
    fun ログインボタンタップ_サーバー接続成功_検索画面に遷移すること() {

        // サンプルEmail
        val sampleMail = "test@test.com"
        // サンプルPassword
        val samplePassword = "12345678"

        onView(withId(R.id.emailEdit)).perform(replaceText(sampleMail))
        onView(withId(R.id.passwordEdit)).perform(replaceText(samplePassword))
        onView(withId(R.id.login_button)).perform(click())

        verify {
            navController.navigate(
                LoginFragmentDirections.actionLoginFragmentToSearchFragment()
            )
        }

    }

    @Test
    fun ログインボタンタップ_ログイン失敗_エラーメッセージが表示されること() {

        // サンプルEmail
        val sampleMail = "test@test.com"
        // サンプルPassword
        val samplePassword = "12345678"


        // 発生させるExceptionを生成
        val exception = HttpException(
            Response.error<Any>(
                400, ResponseBody.create(
                    MediaType.parse("application/json"),
                    "{}"
                )
            )
        )

       every {
           runBlocking {
               repository.loginJigokumimi(any(), any())
           }
       } throws exception

        onView(withId(R.id.emailEdit)).perform(replaceText(sampleMail))
        onView(withId(R.id.passwordEdit)).perform(replaceText(samplePassword))
        onView(withId(R.id.login_button)).perform(click())

        onView(withId(R.id.title_text)).check(matches(isDisplayed()));
    }

    @Test
    fun 無効なメールアドレス_8文字以上のパスワード_ログインボタンが非活性となること() {

        // サンプルEmail(無効メールアドレス)
        val sampleMail = "testtest.com"
        // サンプルPassword
        val samplePassword = "12345678"

        onView(withId(R.id.emailEdit)).perform(replaceText(sampleMail))
        onView(withId(R.id.passwordEdit)).perform(replaceText(samplePassword))
        onView(withId(R.id.login_button)).check(matches(not(isEnabled())))
    }

    @Test
    fun 有効なメールアドレス_8文字未満のパスワード_ログインボタンが非活性となること() {

        // サンプルEmail
        val sampleMail = "test@test.com"
        // サンプルPassword(無効)
        val samplePassword = "1234567"

        onView(withId(R.id.emailEdit)).perform(replaceText(sampleMail))
        onView(withId(R.id.passwordEdit)).perform(replaceText(samplePassword))
        onView(withId(R.id.login_button)).check(matches(not(isEnabled())))
    }

    @Test
    fun サインアップボタンタップ_新規登録画面に遷移すること() {

        onView(withId(R.id.signup_button)).perform(click())

        verify {
            navController.navigate(
                LoginFragmentDirections.actionLoginFragmentToSignUpFragment()
            )
        }
    }
}