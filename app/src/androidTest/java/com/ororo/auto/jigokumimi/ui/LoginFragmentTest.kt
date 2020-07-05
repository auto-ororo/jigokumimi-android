package com.ororo.auto.jigokumimi.ui

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.viewmodels.LoginViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidApplication
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module
import retrofit2.HttpException
import retrofit2.Response

@MediumTest
@RunWith(AndroidJUnit4::class)
class LoginFragmentTest {

    private val repository: IAuthRepository = mockk(relaxed = true)
    private lateinit var navController: NavController

    private val modules: List<Module> = listOf(
        module {
            factory { repository }
        },
        module {
            factory { LoginViewModel(androidApplication(), get()) }
        }
    )

    @Before
    fun init() {

        loadKoinModules(modules)

        // ログイン画面を起動し、NavControllerを設定
        val scenario = launchFragmentInContainer<LoginFragment>(null, R.style.AppTheme)
        navController = mockk<NavController>(relaxed = true)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
    }

    @After
    fun cleanUp() {
        unloadKoinModules(modules)
    }

    @Test
    fun ログインボタンタップ_ログイン成功_エラーメッセージが表示されないこと() {

        // サンプルEmail
        val sampleMail = "test@test.com"
        // サンプルPassword
        val samplePassword = "12345678"

        onView(withId(R.id.emailEdit)).perform(replaceText(sampleMail))
        onView(withId(R.id.passwordEdit)).perform(replaceText(samplePassword))
        onView(withId(R.id.login_button)).perform(click())

        onView(withId(R.id.titleText)).check(doesNotExist());
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

        onView(withId(R.id.titleText)).check(matches(isDisplayed()));
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
    fun Demoボタンタップ_テスト用ユーザーのメールアドレスとパスワードが設定されること() {

        onView(withId(R.id.demoButton)).perform(click())

        onView(withId(R.id.emailEdit)).check(matches(withText("demo@demo.com")))
        onView(withId(R.id.passwordEdit)).check(matches(withText("demodemo")))

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