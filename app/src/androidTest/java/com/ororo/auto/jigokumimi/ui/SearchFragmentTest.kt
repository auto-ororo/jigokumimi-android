package com.ororo.auto.jigokumimi.ui

import ServiceLocator
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.internal.util.Checks
import androidx.test.platform.app.InstrumentationRegistry
import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.network.CommonResponse
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.repository.ILocationRepository
import com.ororo.auto.jigokumimi.repository.IMusicRepository
import com.ororo.auto.jigokumimi.util.Constants
import com.ororo.auto.jigokumimi.util.CreateAndroidTestDataUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.HttpException
import retrofit2.Response
import java.util.*


@MediumTest
@RunWith(AndroidJUnit4::class)
class SearchFragmentTest {

    private lateinit var authRepository: IAuthRepository
    private lateinit var musicRepository: IMusicRepository
    private lateinit var locationRepository: ILocationRepository
    private lateinit var navController: NavController

    val faker = Faker(Locale("jp_JP"))

    private val testDataUtil = CreateAndroidTestDataUtil()

    @Before
    fun init() {
        // mock化したrepositoryをServiceLocatorへ登録し、テスト実行時に参照するように設定)
        authRepository = mockk(relaxed = true)
        musicRepository = mockk(relaxed = true)
        locationRepository = mockk(relaxed = true)
        ServiceLocator.authRepository = authRepository
        ServiceLocator.musicRepository = musicRepository
        ServiceLocator.locationRepository = locationRepository

        // 検索画面を起動し、NavControllerを設定
        val scenario = launchFragmentInContainer<SearchFragment>(null, R.style.AppTheme)
        navController = mockk<NavController>(relaxed = true)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        //  処理を通すためにRepositoryモックの振る舞いを定義
        every { runBlocking { musicRepository.getMyFavoriteTracks() } } returns testDataUtil.createDummyGetMyFavoriteTracksResponse()
        every { runBlocking { musicRepository.postMyFavoriteTracks(any()) } } returns CommonResponse(
            faker.lorem().word(),
            null
        )
        every { runBlocking { musicRepository.refreshTracks(any(), any(), any()) } } returns Unit
        every { runBlocking { authRepository.getSpotifyUserProfile() } } returns testDataUtil.createDummySpotifyUserResponse()
        every { runBlocking { locationRepository.getCurrentLocation() } } returns callbackFlow {
            val location = Location("test")
            location.latitude = faker.number().randomDouble(10, 2, 5)
            location.longitude = faker.number().randomDouble(10, 2, 5)
            offer(location)
            awaitClose {
            }
        }
    }

    @Test
    fun Trackタップ_検索ボタンタップ_Trackの検索処理が実行されること() {

        // Track選択
        onView(withId(R.id.trackText)).perform(click())

        // Trackが選択状態になり、Artistが非選択状態になることを確認
        onView(withId(R.id.trackText)).check(matches(withBackGround(R.color.colorPrimary)))
        onView(withId(R.id.artistButton)).check(matches(withBackGround(R.color.colorGrey)))

        onView(withId(R.id.searchTracksButton)).perform(click())

        verify {
            runBlocking {
                musicRepository.getMyFavoriteTracks()
            }
        }
    }

    @Test
    fun Artistタップ_検索ボタンタップ_Artistの検索処理が実行されること() {

        // Artistタップ
        onView(withId(R.id.artistButton)).perform(click())

        // Trackが非選択状態になり、Artistが選択状態になることを確認
        onView(withId(R.id.trackText)).check(matches(withBackGround(R.color.colorGrey)))
        onView(withId(R.id.artistButton)).check(matches(withBackGround(R.color.colorPrimary)))

        // 検索ボタンタップ
        onView(withId(R.id.searchTracksButton)).perform(click())

        // Artistが検索されることを確認
        verify {
            runBlocking {
                musicRepository.getMyFavoriteArtists()
            }
        }
    }

    @Test
    fun Track選択_距離選択_検索ボタンタップ_スピナーで選択した距離内でのTrack検索が行われ結果画面に遷移すること() {

        // Track選択
        onView(withId(R.id.trackText)).perform(click())

        // スピナーで500mを選択
        onView(withId(R.id.distanceSpinner)).perform(click())
        onView(withText("500m")).perform(click())

        // 検索ボタンタップ
        onView(withId(R.id.searchTracksButton)).perform(click())

        // スピナーで選択した距離(500m)でTrack検索が行われていることを確認
        verify {
            runBlocking {
                musicRepository.refreshTracks(any(), any(), 500)
            }
        }

        // 結果画面に遷移することを確認
        verify {
            navController.navigate(
                SearchFragmentDirections.actionSearchFragmentToResultFragment(Constants.SearchType.TRACK)
            )
        }
    }

    @Test
    fun 検索ボタンタップ_サーバー通信エラー発生_エラーメッセージが表示されること() {

        // 発生させるExceptionを生成
        val exception = HttpException(
            Response.error<Any>(
                404, ResponseBody.create(
                    MediaType.parse("application/json"),
                    "{}"
                )
            )
        )

        // 例外を発生させるように設定
        every {
            runBlocking { locationRepository.getCurrentLocation() }
        } throws exception

        onView(withId(R.id.searchTracksButton)).perform(click())
        onView(withId(R.id.titleText)).check(matches(isDisplayed()))
    }

    @Test
    fun 検索ボタンタップ_トークン認証切れ_ログイン画面に遷移すること() {

        // 発生させるExceptionを生成
        val exception = HttpException(
            Response.error<Any>(
                401, ResponseBody.create(
                    MediaType.parse("application/json"),
                    "{}"
                )
            )
        )

        // 例外を発生させるように設定
        every {
            runBlocking { locationRepository.getCurrentLocation() }
        } throws exception

        // 検索ボタンタップ
        onView(withId(R.id.searchTracksButton)).perform(click())

        // 表示されたエラーメッセージダイアログのOKボタンタップ
        onView(withId(R.id.okButton)).perform(click())

        // ログイン画面に遷移することを確認
        verify {
            navController.navigate(
                R.id.loginFragment
            )
        }
    }

    /**
     * TextViewの背景色を評価するカスタムMatcher
     */
    private fun withBackGround(color: Int): Matcher<View?>? {
        Checks.checkNotNull(color)
        return object : BoundedMatcher<View?, TextView>(TextView::class.java) {
            override fun matchesSafely(warning: TextView): Boolean {
                return ContextCompat.getColor(
                    InstrumentationRegistry.getInstrumentation().targetContext,
                    color
                ) == (warning.background as ColorDrawable).color;
            }

            override fun describeTo(description: Description) {
                description.appendText("with text background: ")
            }
        }
    }
}



