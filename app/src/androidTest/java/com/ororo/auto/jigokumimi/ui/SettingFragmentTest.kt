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
import com.ororo.auto.jigokumimi.viewmodels.SettingViewModel
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
class SettingFragmentTest {
    private lateinit var navController: NavController

    private val repository: IAuthRepository = mockk(relaxed = true)

    private val modules: List<Module> = listOf(
        module {
            factory { repository }
        },
        module {
            factory { SettingViewModel(androidApplication(), get()) }
        }
    )

    @Before
    fun init() {
        loadKoinModules(modules)
        // 検索画面を起動し、NavControllerを設定
        val scenario = launchFragmentInContainer<SettingFragment>(null, R.style.AppTheme)
        navController = mockk(relaxed = true)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
    }

    @After
    fun cleanUp() {
        unloadKoinModules(modules)
    }

    @Test
    fun パスワード変更ボタンタップ_変更成功_新規登録完了Snackbarが表示され検索画面に遷移すること() {

        // 登録フォーム入力
        inputSettingForm(
            sampleCurrentPassword = "87654321",
            sampleNewPassword = "12345678",
            sampleNewPasswordConfirmation = "12345678"
        )

        // パスワード変更ボタンをタップ
        onView(withId(R.id.changePasswordButton)).perform(click())

        // パスワード変更に成功したSnackBarが表示されることを確認
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.success_change_password_message)))

        // 検索画面に遷移することを確認
        verify {
            navController.navigate(
                SettingFragmentDirections.actionSettingFragmentToSearchFragment()
            )
        }
    }

    @Test
    fun パスワード変更ボタンタップ_登録失敗_エラーメッセージが表示されること() {

        val sampleCurrentPassword = "87654321"
        val sampleNewPassword = "12345678"
        val sampleNewPasswordConfirmation = "12345678"

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
                repository.changeJigokumimiPassword(any())
            }
        } throws exception

        // 登録フォーム入力
        inputSettingForm(
            sampleCurrentPassword,
            sampleNewPassword,
            sampleNewPasswordConfirmation
        )

        // パスワード変更ボタンタップ
        onView(withId(R.id.changePasswordButton)).perform(click())

        // エラーメッセージが表示されることを確認
        onView(withId(R.id.titleText)).check(matches(isDisplayed()));
    }

    @Test
    fun 登録解除ボタンタップ_確認ダイアログOKタップ_登録解除完了Snackbarが表示されログイン画面に遷移すること() {

        // 登録解除ボタンをタップ
        onView(withId(R.id.unregisterButton)).perform(click())

        // 確認ダイアログでOKボタンをタップ
        onView(withId(R.id.okButton)).perform(click())


        // 登録解除に成功したSnackBarが表示されることを確認
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.success_unregister_message)))

        // ログイン画面に遷移することを確認
        verify {
            navController.navigate(
                SettingFragmentDirections.actionSettingFragmentToLoginFragment()
            )
        }
    }

    @Test
    fun 登録解除ボタンタップ_確認ダイアログキャンセルタップ_登録解除完了Snackbarが表示されないこと() {

        // 登録解除ボタンをタップ
        onView(withId(R.id.unregisterButton)).perform(click())

        // 確認ダイアログでキャンセルボタンをタップ
        onView(withId(R.id.cancelButton)).perform(click())

        // SnackBarが表示されないことを確認
        onView(withId(com.google.android.material.R.id.snackbar_text))
        onView(withId(R.id.titleText)).check(doesNotExist());

        // ログイン画面に遷移しないことを確認
        verify(exactly = 0) {
            navController.navigate(
                SettingFragmentDirections.actionSettingFragmentToLoginFragment()
            )
        }
    }


    @Test
    fun 登録解除ボタンタップ_登録解除失敗_エラーメッセージが表示されること() {


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
                repository.unregisterJigokumimiUser()
            }
        } throws exception

        // 登録解除ボタンをタップ
        onView(withId(R.id.unregisterButton)).perform(click())

        // 確認ダイアログでOKボタンをタップ
        onView(withId(R.id.okButton)).perform(click())


        // エラーメッセージが表示されることを確認
        onView(withId(R.id.titleText)).check(matches(isDisplayed()));
    }

    @Test
    fun 現在のパスワードが8文字未満_パスワード変更ボタンが非活性となること() {

        // 登録フォーム入力
        inputSettingForm(
            sampleCurrentPassword = "1234567",
            sampleNewPassword = "12345678",
            sampleNewPasswordConfirmation = "12345678"
        )

        // パスワード変更ボタンが非活性になることを確認
        onView(withId(R.id.changePasswordButton)).check(matches(not(isEnabled())))
    }

    @Test
    fun 新パスワードが8文字未満_パスワード変更ボタンが非活性となること() {

        // 登録フォーム入力
        inputSettingForm(
            sampleCurrentPassword = "12345679",
            sampleNewPassword = "1234567",
            sampleNewPasswordConfirmation = "1234567"
        )

        // パスワード変更ボタンが非活性になることを確認
        onView(withId(R.id.changePasswordButton)).check(matches(not(isEnabled())))
    }

    @Test
    fun パスワードが確認用パスワードと異なる_パスワード変更ボタンが非活性となること() {

        // 登録フォーム入力
        inputSettingForm(
            sampleCurrentPassword = "23456789",
            sampleNewPassword = "12345678",
            sampleNewPasswordConfirmation = "87654321"
        )

        // パスワード変更ボタンが非活性になることを確認
        onView(withId(R.id.changePasswordButton)).check(matches(not(isEnabled())))
    }

    /**
     * フォーム入力
     */
    private fun inputSettingForm(
        sampleCurrentPassword: String,
        sampleNewPassword: String,
        sampleNewPasswordConfirmation: String
    ) {
        onView(withId(R.id.currentPasswordEdit)).perform(replaceText(sampleCurrentPassword))
        onView(withId(R.id.newPasswordEdit)).perform(replaceText(sampleNewPassword))
        onView(withId(R.id.newPasswordConfirmationEdit)).perform(
            replaceText(
                sampleNewPasswordConfirmation
            )
        )
    }

}