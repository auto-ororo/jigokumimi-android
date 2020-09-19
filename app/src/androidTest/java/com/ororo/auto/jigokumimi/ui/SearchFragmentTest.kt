package com.ororo.auto.jigokumimi.ui

import android.location.Location
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.network.CommonResponse
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.repository.ILocationRepository
import com.ororo.auto.jigokumimi.repository.IMusicRepository
import com.ororo.auto.jigokumimi.ui.search.SearchFragment
import com.ororo.auto.jigokumimi.util.Constants
import com.ororo.auto.jigokumimi.util.CreateAndroidTestDataUtil
import com.ororo.auto.jigokumimi.ui.search.SearchViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.ResponseBody
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
import java.util.*


@MediumTest
@RunWith(AndroidJUnit4::class)
class SearchFragmentTest {

    private lateinit var navController: NavController

    private val authRepository: IAuthRepository = mockk(relaxed = true)
    private val musicRepository: IMusicRepository = mockk(relaxed = true)
    private val locationRepository: ILocationRepository = mockk(relaxed = true)

    private val modules: List<Module> = listOf(
        module {
            factory { musicRepository }
        },
        module {
            factory { authRepository }
        },
        module {
            factory { locationRepository }
        },
        module {
            factory {
                SearchViewModel(
                    androidApplication(),
                    get(),
                    get(),
                    get()
                )
            }
        }
    )

    val faker = Faker(Locale("jp_JP"))

    private val testDataUtil = CreateAndroidTestDataUtil()

    @Before
    fun init() {
        loadKoinModules(modules)

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

    @After
    fun cleanup() {
        unloadKoinModules(modules)
    }

    @Test
    fun Trackタップ_検索ボタンタップ_Trackの検索処理が実行されること() {

        // Track選択
        onView(withId(R.id.trackButton)).perform(click())

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
        onView(withId(R.id.trackButton)).perform(click())

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
                SearchFragmentDirections.actionSearchFragmentToResultFragment(
                    Constants.Type.TRACK,
                    500,
                    any()
                )
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
}



