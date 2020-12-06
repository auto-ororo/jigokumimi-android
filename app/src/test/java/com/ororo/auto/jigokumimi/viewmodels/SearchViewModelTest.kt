package com.ororo.auto.jigokumimi.viewmodels

import android.location.Location
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.domain.Artist
import com.ororo.auto.jigokumimi.domain.Track
import com.ororo.auto.jigokumimi.network.GetMyFavoriteArtistsResponse
import com.ororo.auto.jigokumimi.network.GetMyFavoriteTracksResponse
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.repository.ILocationRepository
import com.ororo.auto.jigokumimi.repository.IMusicRepository
import com.ororo.auto.jigokumimi.repository.faker.FakeMusicRepository
import com.ororo.auto.jigokumimi.ui.search.SearchViewModel
import com.ororo.auto.jigokumimi.util.CreateTestDataUtil
import getOrAwaitValue
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.core.IsEqual
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import java.util.*

//@RunWith(AndroidJUnit4::class)
//class SearchViewModelTest {
//
//    // LiveDataのテストに必要なルールを設定
//    @get:Rule
//    var instantExecutorRule = InstantTaskExecutorRule()
//
//    lateinit var viewModel: SearchViewModel
//    lateinit var authRepository: IAuthRepository
//    lateinit var musicRepository: IMusicRepository
//    lateinit var locationRepository: ILocationRepository
//    lateinit var location: Location
//    lateinit var getMyFavoriteTracksResponse: GetMyFavoriteTracksResponse
//    lateinit var getMyFavoriteArtistsResponse: GetMyFavoriteArtistsResponse
//    lateinit var jigokumimiUserId: String
//    lateinit var tracks: MutableList<Track>
//    lateinit var artists: MutableList<Artist>
//
//    val faker = Faker(Locale("jp_JP"))
//
//    val testDataUtil = CreateTestDataUtil()
//
//    @Before
//    fun createViewModel() {
//
//        getMyFavoriteArtistsResponse = testDataUtil.createDummyGetMyFavoriteArtistsResponse()
//        getMyFavoriteTracksResponse = testDataUtil.createDummyGetMyFavoriteTracksResponse()
//
//        jigokumimiUserId = faker.random().hex()
//
//
//
//        location = Location("test")
//        location.latitude = faker.number().randomDouble(10, 2, 5)
//        location.longitude = faker.number().randomDouble(10, 2, 5)
//
//        tracks = mutableListOf()
//        artists = mutableListOf()
//
//        for (i in 1..3) {
//            tracks.add(testDataUtil.createDummyTrack())
//            artists.add(testDataUtil.createDummyArtist())
//        }
//
//        authRepository = mockk(relaxed = true)
//        musicRepository = spyk(FakeMusicRepository(_tracks = tracks, _artists = artists))
//        locationRepository = mockk(relaxed = true)
//
//        every { runBlocking { locationRepository.getCurrentLocation() } } returns callbackFlow {
//            offer(location)
//            awaitClose {
//            }
//        }
//
//        every { runBlocking { authRepository.getSavedJigokumimiUserId() } } returns jigokumimiUserId
//        every { runBlocking { musicRepository.getMyFavoriteArtists() } } returns getMyFavoriteArtistsResponse
//        every { runBlocking { musicRepository.getMyFavoriteTracks() } } returns getMyFavoriteTracksResponse
//
//        viewModel =
//            spyk(
//                SearchViewModel(
//                    ApplicationProvider.getApplicationContext(),
//                    authRepository,
//                    musicRepository,
//                    locationRepository
//                )
//            )
//    }
//
//    @After
//    fun shutDownWebServer() {
//        stopKoin()
//    }
//
//    @Test
//    fun searchMusic_Artist_アーティスト情報が送受信され検索完了フラグがtrueになること() = runBlockingTest {
//
//        // アーティストに設定
//        viewModel.setSearchTypeToArtist()
//        // 距離を設定
//        viewModel.setDistanceFromSelectedSpinnerString("100m")
//        // メソッド呼び出し
//        viewModel.searchMusic()
//
//        // 処理内のRepositoryメソッドが呼ばれることを確認
//        verify { runBlocking { locationRepository.getCurrentLocation() } }
//        verify { runBlocking { authRepository.getSavedJigokumimiUserId() } }
//        verify { runBlocking { musicRepository.getMyFavoriteArtists() } }
//        verify { runBlocking { musicRepository.shouldPostFavoriteArtists() } }
//        verify {
//            runBlocking {
//                musicRepository.postMyFavoriteArtists(
//                    getMyFavoriteArtistsResponse.asPostMyFavoriteArtistsRequest(
//                        jigokumimiUserId,
//                        location
//                    )
//                )
//            }
//        }
//        verify { runBlocking { musicRepository.refreshArtists(jigokumimiUserId, location, any()) } }
//
//        // 検索完了フラグがTrueになることを確認
//        assertThat(viewModel.isSearchFinished.getOrAwaitValue(), IsEqual(true))
//    }
//
//    @Test
//    fun searchMusic_Artist_前回送信日時から一定時間経過していない_アーティスト情報が送信されないこと() = runBlockingTest {
//
//        musicRepository =
//            spyk(FakeMusicRepository(_tracks = tracks, _artists = artists, shouldPostMusic = false))
//
//        // アーティストに設定
//        viewModel.setSearchTypeToArtist()
//        // 距離を設定
//        viewModel.setDistanceFromSelectedSpinnerString("100m")
//        // メソッド呼び出し
//        viewModel.searchMusic()
//
//        // Repositoryメソッドが呼ばれないことを確認
//        verify(exactly = 0) { runBlocking { musicRepository.getMyFavoriteArtists() } }
//        verify(exactly = 0) {
//            runBlocking {
//                musicRepository.postMyFavoriteArtists(
//                    getMyFavoriteArtistsResponse.asPostMyFavoriteArtistsRequest(
//                        jigokumimiUserId,
//                        location
//                    )
//                )
//            }
//        }
//    }
//
//    @Test
//    fun searchMusic_Track_曲情報が送受信され検索完了フラグがtrueになること() = runBlocking {
//
//        // Trackに設定
//        viewModel.setSearchTypeToTrack()
//        // 距離を設定
//        viewModel.setDistanceFromSelectedSpinnerString("100m")
//        // メソッド呼び出し
//        viewModel.searchMusic()
//
//        // 処理内のRepositoryメソッドが呼ばれることを確認
//        verify { runBlocking { locationRepository.getCurrentLocation() } }
//        verify { runBlocking { authRepository.getSavedJigokumimiUserId() } }
//        verify { runBlocking { musicRepository.getMyFavoriteTracks() } }
//        verify { runBlocking { musicRepository.shouldPostFavoriteTracks() } }
//        verify {
//            runBlocking {
//                musicRepository.postMyFavoriteTracks(
//                    getMyFavoriteTracksResponse.asPostMyFavoriteTracksRequest(
//                        jigokumimiUserId,
//                        location
//                    )
//                )
//            }
//        }
//        verify { runBlocking { musicRepository.refreshTracks(jigokumimiUserId, location, 100) } }
//
//        // 検索完了フラグがTrueになることを確認
//        assertThat(viewModel.isSearchFinished.getOrAwaitValue(), IsEqual(true))
//    }
//
//    @Test
//    fun searchMusic_Track_前回送信日時から一定時間経過していない_曲情報が送信されないこと() = runBlocking {
//
//        musicRepository =
//            spyk(FakeMusicRepository(_tracks = tracks, _artists = artists, shouldPostMusic = false))
//
//        // トラックに設定
//        viewModel.setSearchTypeToTrack()
//        // 距離を設定
//        viewModel.setDistanceFromSelectedSpinnerString("100m")
//        // メソッド呼び出し
//        viewModel.searchMusic()
//
//        // Repositoryメソッドが呼ばれないことを確認
//        verify(exactly = 0) { runBlocking { musicRepository.getMyFavoriteTracks() } }
//        verify(exactly = 0) {
//            runBlocking {
//                musicRepository.postMyFavoriteTracks(
//                    getMyFavoriteTracksResponse.asPostMyFavoriteTracksRequest(
//                        jigokumimiUserId,
//                        location
//                    )
//                )
//            }
//        }
//    }
//
//    @Test
//    fun searchMusic_例外発生_エラーメッセージが設定され検索完了フラグがfalseになること() = runBlocking {
//
//        // 発生させるExceptionを生成
//        val exception = Exception()
//
//        every { runBlocking { authRepository.getSavedJigokumimiUserId() } } throws exception
//
//        viewModel.searchMusic()
//
//        verify { viewModel.handleConnectException(exception) }
//    }
//
//    @Test
//    fun setDistanceFromSelectedSpinnerString_末尾文字列mが切り取られInt型に変換されること() {
//
//        val testData = "100m"
//
//        viewModel.setDistanceFromSelectedSpinnerString(testData)
//
//        assertThat(viewModel.distance.value, IsEqual(100))
//    }
//
//}