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
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.HttpException
import retrofit2.Response

@MediumTest
@RunWith(AndroidJUnit4::class)
class SighUpFragmentTest {

    private lateinit var repository: IAuthRepository
    private lateinit var navController: NavController

    @Before
    fun init() {
        // mock化したrepositoryをServiceLocatorへ登録し、テスト実行時に参照するように設定)
        repository = mockk(relaxed = true)
        ServiceLocator.authRepository = repository

        // 検索画面を起動し、NavControllerを設定
        val scenario = launchFragmentInContainer<SignUpFragment>(null, R.style.AppTheme)
        navController = mockk(relaxed = true)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
    }

    @Test
    fun サインアップボタンタップ_新規登録成功_OKダイアログタップ後検索画面に遷移すること() {

        // 登録フォーム入力
        inputSignUpForm(
            sampleName = "hogehoge",
            sampleMail = "test@test.com",
            samplePassword = "12345678",
            samplePasswordConfirmation = "12345678"
        )

        // サインアップボタンをタップ
        onView(withId(R.id.sign_up_button)).perform(click())

        // 新規登録に成功したダイアログのOKをタップ
        onView(withId(R.id.ok_button)).perform(click())

        // 検索画面に遷移することを確認
        verify {
            navController.navigate(SignUpFragmentDirections.actionSignUpFragmentToSearchFragment())
        }
    }

    @Test
    fun サインアップボタンタップ_登録失敗_エラーメッセージが表示されること() {

        val sampleName = "hogehoge"
        val sampleMail = "test@test.com"
        val samplePassword = "12345678"
        val samplePasswordConfirmation = "12345678"

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
                repository.signUpJigokumimi(any())
            }
        } throws exception

        // 登録フォーム入力
        inputSignUpForm(
            sampleName,
            sampleMail,
            samplePassword,
            samplePasswordConfirmation
        )

        // サインアップボタンタップ
        onView(withId(R.id.sign_up_button)).perform(click())

        // エラーメッセージが表示されることを確認
        onView(withId(R.id.title_text)).check(matches(isDisplayed()));
    }

    @Test
    fun 名前が空文字_サインアップボタンが非活性となること() {

        // 登録フォーム入力
        inputSignUpForm(
            sampleName = "",
            sampleMail = "test@test.com",
            samplePassword = "12345678",
            samplePasswordConfirmation = "12345678"
        )

        // サインアップボタンが非活性になることを確認
        onView(withId(R.id.sign_up_button)).check(matches(not(isEnabled())))
    }

    @Test
    fun メールアドレスが不正_サインアップボタンが非活性となること() {

        // 登録フォーム入力
        inputSignUpForm(
            sampleName = "hogehoge",
            sampleMail = "testcom",
            samplePassword = "12345678",
            samplePasswordConfirmation = "12345678"
        )

        // サインアップボタンが非活性になることを確認
        onView(withId(R.id.sign_up_button)).check(matches(not(isEnabled())))
    }

    @Test
    fun パスワードが8文字未満_サインアップボタンが非活性となること() {

        // 登録フォーム入力
        inputSignUpForm(
            sampleName = "hogehoge",
            sampleMail = "test@test.com",
            samplePassword = "1234567",
            samplePasswordConfirmation = "1234567"
        )

        // サインアップボタンが非活性になることを確認
        onView(withId(R.id.sign_up_button)).check(matches(not(isEnabled())))
    }

    @Test
    fun パスワードが確認用パスワードと異なる_サインアップボタンが非活性となること() {

        // 登録フォーム入力
        inputSignUpForm(
            sampleName = "hogehoge",
            sampleMail = "test@test.com",
            samplePassword = "12345678",
            samplePasswordConfirmation = "87654321"
        )

        // サインアップボタンが非活性になることを確認
        onView(withId(R.id.sign_up_button)).check(matches(not(isEnabled())))
    }

    /**
     * フォーム入力
     */
    private fun inputSignUpForm(
        sampleName: String,
        sampleMail: String,
        samplePassword: String,
        samplePasswordConfirmation: String
    ) {
        onView(withId(R.id.nameEdit)).perform(replaceText(sampleName))
        onView(withId(R.id.emailEdit)).perform(replaceText(sampleMail))
        onView(withId(R.id.passwordEdit)).perform(replaceText(samplePassword))
        onView(withId(R.id.passwordConfirmationEdit)).perform(replaceText(samplePasswordConfirmation))
    }

}