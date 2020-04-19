package com.ororo.auto.jigokumimi.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.R
import com.ororo.auto.jigokumimi.domain.Artist
import com.ororo.auto.jigokumimi.domain.Track
import com.ororo.auto.jigokumimi.repository.faker.FakeAuthRepository
import com.ororo.auto.jigokumimi.repository.faker.FakeLocationRepository
import com.ororo.auto.jigokumimi.repository.faker.FakeMusicRepository
import com.ororo.auto.jigokumimi.util.CreateTestDataUtil
import getOrAwaitValue
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.hamcrest.core.IsEqual
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class SearchViewModelTest {

    // LiveDataのテストに必要なルールを設定
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    lateinit var viewModel: SearchViewModel
    lateinit var authRepository: FakeAuthRepository
    lateinit var musicRepository: FakeMusicRepository
    lateinit var locationRepository: FakeLocationRepository


    val faker = Faker(Locale("jp_JP"))

    val testDataUtil = CreateTestDataUtil()

    @Before
    fun createViewModel() {

        val tracks = mutableListOf<Track>()
        val artists = mutableListOf<Artist>()

        for (i in 1..3) {
            tracks.add(testDataUtil.createDummyTrack())
            artists.add(testDataUtil.createDummyArtist())
        }

        val latitude = faker.number().randomDouble(10, 2, 6)
        val longitude = faker.number().randomDouble(10, 2, 6)

        authRepository = spyk(FakeAuthRepository())
        musicRepository = spyk(FakeMusicRepository(_tracks = tracks, _artists = artists))
        locationRepository = spyk(FakeLocationRepository(latitude, longitude))

        viewModel = spyk(
            SearchViewModel(
                ApplicationProvider.getApplicationContext(),
                authRepository,
                musicRepository,
                locationRepository
            )
        )
    }

    @Test
    fun searchMusic_Artist_アーティスト情報が送受信され検索完了フラグがtrueになること() = runBlocking {

        // アーティストに設定
        viewModel.setSearchTypeToArtist()
        // メソッド呼び出し
        viewModel.searchMusic()

        // 処理内のRepositoryメソッドが呼ばれることを確認
        verify { runBlocking { locationRepository.getCurrentLocation() } }
        verify { runBlocking { authRepository.getSavedJigokumimiUserId() } }
        verify { runBlocking { musicRepository.getMyFavoriteArtists() } }
        verify { runBlocking { musicRepository.postMyFavoriteArtists(any()) } }
        verify { runBlocking { musicRepository.refreshArtists(any(), any(), any()) } }


        // 検索完了フラグがTrueになることを確認
        assertThat(viewModel.isSearchFinished.getOrAwaitValue(), IsEqual(true))
    }

    @Test
    fun searchMusic_Track_曲情報が送受信され検索完了フラグがtrueになること() = runBlocking {

        viewModel.setSearchTypeToTrack()
        viewModel.searchMusic()

        // 処理内のRepositoryメソッドが呼ばれることを確認
        verify { runBlocking { locationRepository.getCurrentLocation() } }
        verify { runBlocking { authRepository.getSavedJigokumimiUserId() } }
        verify { runBlocking { musicRepository.getMyFavoriteTracks() } }
        verify { runBlocking { musicRepository.postMyFavoriteTracks(any()) } }
        verify { runBlocking { musicRepository.refreshTracks(any(), any(), any()) } }

        // 検索完了フラグがTrueになることを確認
        assertThat(viewModel.isSearchFinished.getOrAwaitValue(), IsEqual(true))
    }

    @Test
    fun searchMusic_例外発生_エラーメッセージが設定され検索完了フラグがfalseになること() = runBlocking {

        // 発生させるExceptionを生成
        val exception = Exception()

        val latitude = faker.number().randomDouble(10, 2, 6)
        val longitude = faker.number().randomDouble(10, 2, 6)

        viewModel = SearchViewModel(
            ApplicationProvider.getApplicationContext(),
            FakeAuthRepository(),
            FakeMusicRepository(exception = exception),
            FakeLocationRepository(latitude, longitude)
        )

        viewModel.searchMusic()

        val extectedMessage =
            InstrumentationRegistry.getInstrumentation().context.resources.getString(
                R.string.general_error_message, exception::class.java
            )

        assertThat(viewModel.errorMessage.value, IsEqual(extectedMessage))
        assertThat(viewModel.isErrorDialogShown.value, IsEqual(true))
        assertThat(viewModel.isSearchFinished.value, IsEqual(false))
    }

    @Test
    fun setDistanceFromSelectedSpinnerString_末尾文字列mが切り取られInt型に変換されること() {

        val testData = "100m"

        viewModel.setDistanceFromSelectedSpinnerString(testData)

        assertThat(viewModel.distance.value, IsEqual(100))
    }

}