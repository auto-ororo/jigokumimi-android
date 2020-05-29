package com.ororo.auto.jigokumimi.repository.test

import android.content.SharedPreferences
import android.location.Location
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.domain.Artist
import com.ororo.auto.jigokumimi.domain.History
import com.ororo.auto.jigokumimi.domain.HistoryItem
import com.ororo.auto.jigokumimi.domain.Track
import com.ororo.auto.jigokumimi.network.*
import com.ororo.auto.jigokumimi.repository.MusicRepository
import com.ororo.auto.jigokumimi.util.Constants
import com.ororo.auto.jigokumimi.util.CreateTestDataUtil
import com.ororo.auto.jigokumimi.util.MockkHelper.Companion.any
import getOrAwaitValue
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot
import org.hamcrest.core.IsNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import kotlin.collections.HashMap

@RunWith(AndroidJUnit4::class)
class MusicRepositoryTest {
    lateinit var prefData: SharedPreferences
    lateinit var jigokumimiApiService: FakeJigokumimiApiService
    lateinit var spotifyApiService: FakeSpotifyApiService
    lateinit var musicRepository: MusicRepository
    lateinit var faker: Faker
    lateinit var ct: CreateTestDataUtil

    // LiveDataのテストに必要なルールを設定
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createRepository() {
        prefData =
            PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
        jigokumimiApiService = spyk(FakeJigokumimiApiService())
        spotifyApiService = spyk(FakeSpotifyApiService())
        // Get a reference to the class under test
        musicRepository = MusicRepository(
            prefData,
            spotifyApiService,
            jigokumimiApiService
        )
        faker = Faker(Locale("ja_JP"))
        ct = CreateTestDataUtil()
    }

    @Test
    fun refreshTracks_周辺曲情報と詳細曲情報を元にLivedataが更新されること() = runBlocking {
        // テスト対象のメソッドの引数を設定
        val spotifyUserId = faker.random().hex()
        val location = Location("test")
        location.latitude = faker.number().randomDouble(10, 2, 5)
        location.longitude = faker.number().randomDouble(10, 2, 5)
        val distance = faker.number().randomDigit()


        // ダミーデータ、及び期待されるDBレコードを作成
        val tracksAroundNetWork: MutableList<TrackAroundNetwork> = mutableListOf()
        val tracksDetail: MutableList<GetTrackDetailResponse> = mutableListOf()
        val expectedTrackList: MutableList<Track> = mutableListOf()
        val trackSavedList: HashMap<String, Boolean> = HashMap()
        for (i in 1..10) {
            // 曲情報を紐付けるID
            // このIDを元にJigokumimiから取得する周辺曲情報、Spotifyから取得する曲詳細情報、ローカルDBに保存する曲情報を紐つける
            val spotifyTrackId = faker.random().hex()
            // 周辺曲情報を作成
            tracksAroundNetWork.add(
                ct.createDummyTrackAroundNetwork(rank = i, spotifyTrackId = spotifyTrackId)
            )
            // 曲詳細情報を作成
            tracksDetail.add(
                ct.createDummyGetTrackDetailResponse(spotifyTrackId)
            )
            // 曲保存情報を生成
            trackSavedList[spotifyTrackId] = faker.bool().bool()
            // メソッド呼び出し後に期待されるDBのレコードを作成
            expectedTrackList.add(
                Track(
                    id = spotifyTrackId,
                    popularity = tracksAroundNetWork.last().popularity,
                    album = tracksDetail.last().album.name,
                    artists = tracksDetail.last().artists.joinToString(separator = ", ") {
                        it.name
                    },
                    imageUrl = tracksDetail.last().album.images[0].url,
                    name = tracksDetail.last().name,
                    previewUrl = tracksDetail.last().previewUrl,
                    rank = tracksAroundNetWork.last().rank,
                    isSaved = trackSavedList[spotifyTrackId]!!,
                    isDeleted = false
                )
            )
        }
        val getTracksAroundResponse = GetTracksAroundResponse(
            data = tracksAroundNetWork,
            message = faker.random().toString()
        )
        jigokumimiApiService.getTracksAroundResponse = getTracksAroundResponse
        spotifyApiService.tracksDetail = tracksDetail
        spotifyApiService.tracksSaveList = trackSavedList
        expectedTrackList.sortBy { it.rank }

        //メソッド呼び出し
        musicRepository.refreshTracks(spotifyUserId, location, distance)

        musicRepository.tracks.getOrAwaitValue().mapIndexed { i, actualTrackAround ->
            assertThat(expectedTrackList[i], IsEqual(actualTrackAround))
        }

        return@runBlocking
    }

    @Test
    fun refreshTracksFromHistory_履歴情報のLivedataが更新されること() = runBlocking {
        // テスト対象のメソッドの引数を設定
        val spotifyUserId = faker.random().hex()
        val location = Location("test")
        location.latitude = faker.number().randomDouble(10, 2, 5)
        location.longitude = faker.number().randomDouble(10, 2, 5)
        val distance = faker.number().randomDigit()

        // ダミーデータ、及び期待されるDBレコードを作成
        val historyItem: MutableList<HistoryItem> = mutableListOf()
        val tracksDetail: MutableList<GetTrackDetailResponse> = mutableListOf()
        val expectedTrackList: MutableList<Track> = mutableListOf()
        val trackSavedList: HashMap<String, Boolean> = HashMap()
        for (i in 1..10) {
            // 曲情報を紐付けるID
            // このIDを元に履歴情報、Spotifyから取得する曲詳細情報、ローカルDBに保存する曲情報を紐つける
            val spotifyTrackId = faker.random().hex()
            // 履歴詳細情報を作成
            historyItem.add(
                ct.createDummyHistoryItem(rank = i, spotifyItemId = spotifyTrackId)
            )
            // 曲詳細情報を作成
            tracksDetail.add(
                ct.createDummyGetTrackDetailResponse(spotifyTrackId)
            )
            // 曲保存情報を生成
            trackSavedList[spotifyTrackId] = faker.bool().bool()
            // メソッド呼び出し後に期待されるDBのレコードを作成
            expectedTrackList.add(
                Track(
                    id = spotifyTrackId,
                    popularity = historyItem.last().popularity,
                    album = tracksDetail.last().album.name,
                    artists = tracksDetail.last().artists.joinToString(separator = ", ") {
                        it.name
                    },
                    imageUrl = tracksDetail.last().album.images[0].url,
                    name = tracksDetail.last().name,
                    previewUrl = tracksDetail.last().previewUrl,
                    rank = historyItem.last().rank,
                    isSaved = trackSavedList[spotifyTrackId]!!,
                    isDeleted = false
                )
            )
        }
        spotifyApiService.tracksDetail = tracksDetail
        spotifyApiService.tracksSaveList = trackSavedList
        expectedTrackList.sortBy { it.rank }

        val history = History(
            spotifyUserId,
            latitude = location.latitude,
            longitude = location.longitude,
            distance = distance,
            place = faker.lorem().characters(),
            historyItems = historyItem,
            createdAt = faker.date().toString()
        )

        //メソッド呼び出し
        musicRepository.refreshTracksFromHistory(history)

        musicRepository.tracks.getOrAwaitValue().mapIndexed { i, actualTrackAround ->
            assertThat(expectedTrackList[i], IsEqual(actualTrackAround))
        }
        return@runBlocking
    }

    @Test
    fun refreshArtists_周辺アーティスト情報と詳細アーティスト情報を元にLivedataが更新されること() = runBlocking {
        // テスト対象のメソッドの引数を設定
        val spotifyUserId = faker.random().hex()
        val location = Location("test")
        location.latitude = faker.number().randomDouble(10, 2, 5)
        location.longitude = faker.number().randomDouble(10, 2, 5)
        val distance = faker.number().randomDigit()


        // ダミーデータ、及び期待されるDBレコードを作成
        val artistsAroundNetWork: MutableList<ArtistAroundNetwork> = mutableListOf()
        val artistsDetail: MutableList<SpotifyArtistFull> = mutableListOf()
        val expectedArtistList: MutableList<Artist> = mutableListOf()
        val artistsFollowList: HashMap<String, Boolean> = HashMap()
        for (i in 1..10) {
            // アーティスト情報を紐付けるID
            // このIDを元にJigokumimiから取得する周辺アーティスト情報、Spotifyから取得するアーティスト詳細情報、ローカルDBに保存するアーティスト情報を紐つける
            val spotifyArtistId = faker.random().hex()
            // 周辺アーティスト情報を作成
            artistsAroundNetWork.add(
                ct.createDummyArtistAroundNetwork(rank = i, spotifyArtistId = spotifyArtistId)
            )
            // アーティスト詳細情報を作成
            artistsDetail.add(
                ct.createDummySpotifyArtistFull(spotifyArtistId)
            )
            // アーティスト保存情報を生成
            artistsFollowList[spotifyArtistId] = faker.bool().bool()
            // メソッド呼び出し後に期待されるDBレコードを作成
            expectedArtistList.add(
                Artist(
                    id = spotifyArtistId,
                    popularity = artistsAroundNetWork.last().popularity,
                    imageUrl = artistsDetail.last().images[0].url,
                    name = artistsDetail.last().name,
                    rank = artistsAroundNetWork.last().rank,
                    genres = artistsDetail.last().genres?.joinToString(separator = ", "),
                    isFollowed = artistsFollowList[spotifyArtistId]!!,
                    isDeleted = false
                )
            )
        }
        val getArtistsAroundResponse = GetArtistsAroundResponse(
            data = artistsAroundNetWork,
            message = faker.random().toString()
        )
        jigokumimiApiService.getArtistsAroundResponse = getArtistsAroundResponse
        spotifyApiService.artistsDetail = artistsDetail
        spotifyApiService.artistsFollowList = artistsFollowList
        expectedArtistList.sortBy { it.rank }

        //メソッド呼び出し
        musicRepository.refreshArtists(spotifyUserId, location, distance)

        // レスポンスが期待値と等しいことを確認
        musicRepository.artists.getOrAwaitValue().mapIndexed { i, actualArtistAround ->
            assertThat(actualArtistAround, IsEqual(expectedArtistList.get(i)))
        }

        return@runBlocking
    }

    @Test
    fun refreshArtistsFromHistory_履歴情報を元にLivedataが更新されること() = runBlocking {
        // テスト対象のメソッドの引数を設定
        val spotifyUserId = faker.random().hex()
        val location = Location("test")
        location.latitude = faker.number().randomDouble(10, 2, 5)
        location.longitude = faker.number().randomDouble(10, 2, 5)
        val distance = faker.number().randomDigit()


        // ダミーデータ、及び期待されるDBレコードを作成
        val historyItems: MutableList<HistoryItem> = mutableListOf()
        val artistsDetail: MutableList<SpotifyArtistFull> = mutableListOf()
        val expectedArtistList: MutableList<Artist> = mutableListOf()
        val artistsFollowList: HashMap<String, Boolean> = HashMap()
        for (i in 1..10) {
            // アーティスト情報を紐付けるID
            // このIDを元にJigokumimiから取得する周辺アーティスト情報、Spotifyから取得するアーティスト詳細情報、ローカルDBに保存するアーティスト情報を紐つける
            val spotifyArtistId = faker.random().hex()
            // 周辺アーティスト情報を作成
            historyItems.add(
                ct.createDummyHistoryItem(rank = i, spotifyItemId = spotifyArtistId)
            )
            // アーティスト詳細情報を作成
            artistsDetail.add(
                ct.createDummySpotifyArtistFull(spotifyArtistId)
            )
            // アーティスト保存情報を生成
            artistsFollowList[spotifyArtistId] = faker.bool().bool()
            // メソッド呼び出し後に期待されるDBレコードを作成
            expectedArtistList.add(
                Artist(
                    id = spotifyArtistId,
                    popularity = historyItems.last().popularity,
                    imageUrl = artistsDetail.last().images[0].url,
                    name = artistsDetail.last().name,
                    rank = historyItems.last().rank,
                    genres = artistsDetail.last().genres?.joinToString(separator = ", "),
                    isFollowed = artistsFollowList[spotifyArtistId]!!,
                    isDeleted = false
                )
            )
        }
        spotifyApiService.artistsDetail = artistsDetail
        spotifyApiService.artistsFollowList = artistsFollowList
        expectedArtistList.sortBy { it.rank }

        val history = History(
            spotifyUserId,
            latitude = location.latitude,
            longitude = location.longitude,
            distance = distance,
            place = faker.lorem().characters(),
            historyItems = historyItems,
            createdAt = faker.date().toString()
        )

        //メソッド呼び出し
        musicRepository.refreshArtistsFromHistory(history)

        // レスポンスが期待値と等しいことを確認
        musicRepository.artists.getOrAwaitValue().mapIndexed { i, actualArtistAround ->
            assertThat(actualArtistAround, IsEqual(expectedArtistList.get(i)))
        }

        return@runBlocking
    }

    @Test
    fun getMyFavoriteTracks_APIリクエストが呼ばれること() = runBlocking {

        // モック化
        jigokumimiApiService = mockk(relaxed = true)
        spotifyApiService = mockk(relaxed = true)
        musicRepository = MusicRepository(prefData, spotifyApiService, jigokumimiApiService)
        every { runBlocking { spotifyApiService.getTracks(any(), any(), any()) } } returns any()

        // メソッド呼び出し
        musicRepository.getMyFavoriteTracks()

        // APIリクエストが呼ばれることを確認
        verify { runBlocking { spotifyApiService.getTracks(any(), any(), any()) } }
    }

    @Test
    fun postMyFavoriteTracks_APIリクエストが呼ばれSharedPreferencesに送信時刻が保存されること() = runBlocking {

        // モック化
        jigokumimiApiService = mockk(relaxed = true)
        spotifyApiService = mockk(relaxed = true)
        musicRepository = MusicRepository(prefData, spotifyApiService, jigokumimiApiService)
        every { runBlocking { jigokumimiApiService.postTracks(any(), any()) } } returns any()

        // メソッド呼び出し
        musicRepository.postMyFavoriteTracks(any())

        // SharedPreferencesに送信時刻が保存されていることを確認
        assertThat(
            prefData.getString(
                Constants.SP_JIGOKUMIMI_POSTED_FAVORITE_TRACKS_DATETIME_KEY,
                ""
            ), IsNot(IsEqual(""))
        )

        // APIリクエストが呼ばれることを確認
        verify { runBlocking { jigokumimiApiService.postTracks(any(), any()) } }
    }

    @Test
    fun getMyFavoriteArtists_APIリクエストが呼ばれること() = runBlocking {

        // モック化
        jigokumimiApiService = mockk(relaxed = true)
        spotifyApiService = mockk(relaxed = true)
        musicRepository = MusicRepository(prefData, spotifyApiService, jigokumimiApiService)
        every { runBlocking { spotifyApiService.getArtists(any(), any(), any()) } } returns any()

        // メソッド呼び出し
        musicRepository.getMyFavoriteArtists()

        // APIリクエストが呼ばれることを確認
        verify { runBlocking { spotifyApiService.getArtists(any(), any(), any()) } }
    }

    @Test
    fun postMyFavoriteArtists_APIリクエストが呼ばれSharedPreferencesに送信時刻が保存されること() = runBlocking {

        // モック化
        jigokumimiApiService = mockk(relaxed = true)
        spotifyApiService = mockk(relaxed = true)
        musicRepository = MusicRepository(prefData, spotifyApiService, jigokumimiApiService)
        every { runBlocking { jigokumimiApiService.postArtists(any(), any()) } } returns any()

        // メソッド呼び出し
        musicRepository.postMyFavoriteArtists(any())

        // SharedPreferencesに送信時刻が保存されていることを確認
        assertThat(
            prefData.getString(
                Constants.SP_JIGOKUMIMI_POSTED_FAVORITE_ARTISTS_DATETIME_KEY,
                ""
            ), IsNot(IsEqual(""))
        )

        // APIリクエストが呼ばれることを確認
        verify { runBlocking { jigokumimiApiService.postArtists(any(), any()) } }
    }


    @Test
    fun getTracksAroundSearchHistories_APIリクエストが呼ばれること() = runBlocking {

        // モック化
        jigokumimiApiService = mockk(relaxed = true)
        spotifyApiService = mockk(relaxed = true)
        musicRepository = MusicRepository(prefData, spotifyApiService, jigokumimiApiService)
        every {
            runBlocking {
                jigokumimiApiService.getTracksAroundSearchHistories(
                    any(),
                    any()
                )
            }
        } returns any()

        // メソッド呼び出し
        musicRepository.getTracksAroundSearchHistories(any())

        // APIリクエストが呼ばれることを確認
        verify { runBlocking { jigokumimiApiService.getTracksAroundSearchHistories(any(), any()) } }
    }

    @Test
    fun getArtistsAroundSearchHistories_APIリクエストが呼ばれること() = runBlocking {

        // モック化
        jigokumimiApiService = mockk(relaxed = true)
        spotifyApiService = mockk(relaxed = true)
        musicRepository = MusicRepository(prefData, spotifyApiService, jigokumimiApiService)
        every {
            runBlocking {
                jigokumimiApiService.getArtistsAroundSearchHistories(
                    any(),
                    any()
                )
            }
        } returns any()

        // メソッド呼び出し
        musicRepository.getArtistsAroundSearchHistories(any())

        // APIリクエストが呼ばれることを確認
        verify {
            runBlocking {
                jigokumimiApiService.getArtistsAroundSearchHistories(
                    any(),
                    any()
                )
            }
        }
    }


    @Test
    fun deleteTracksAroundSearchHistories_APIリクエストが呼ばれること() = runBlocking {

        // モック化
        jigokumimiApiService = mockk(relaxed = true)
        spotifyApiService = mockk(relaxed = true)
        musicRepository = MusicRepository(prefData, spotifyApiService, jigokumimiApiService)
        every {
            runBlocking {
                jigokumimiApiService.deleteTracksAroundSearchHistories(
                    any(),
                    any()
                )
            }
        } returns any()

        // メソッド呼び出し
        musicRepository.deleteTracksAroundSearchHistories(any())

        // APIリクエストが呼ばれることを確認
        verify {
            runBlocking {
                jigokumimiApiService.deleteTracksAroundSearchHistories(
                    any(),
                    any()
                )
            }
        }
    }

    @Test
    fun deleteArtistsAroundSearchHistories_APIリクエストが呼ばれること() = runBlocking {

        // モック化
        jigokumimiApiService = mockk(relaxed = true)
        spotifyApiService = mockk(relaxed = true)
        musicRepository = MusicRepository(prefData, spotifyApiService, jigokumimiApiService)
        every {
            runBlocking {
                jigokumimiApiService.deleteArtistsAroundSearchHistories(
                    any(),
                    any()
                )
            }
        } returns any()

        // メソッド呼び出し
        musicRepository.deleteArtistsAroundSearchHistories(any())

        // APIリクエストが呼ばれることを確認
        verify {
            runBlocking {
                jigokumimiApiService.deleteArtistsAroundSearchHistories(
                    any(),
                    any()
                )
            }
        }
    }

    @Test
    fun shouldPostFavoriteTracks_送信間隔内_falseが返却されること() {

        prefData.edit().let {
            it.putString(
                Constants.SP_JIGOKUMIMI_POSTED_FAVORITE_TRACKS_DATETIME_KEY,
                System.currentTimeMillis().toString()
            )
            it.apply()
        }

        assertThat(musicRepository.shouldPostFavoriteTracks(), IsEqual(false))
    }

    @Test
    fun shouldPostFavoriteTracks_送信間隔外_trueが返却されること() {

        prefData.edit().let {
            it.putString(
                Constants.SP_JIGOKUMIMI_POSTED_FAVORITE_TRACKS_DATETIME_KEY,
                (System.currentTimeMillis() - Constants.POST_MUSIC_PERIOD - 1).toString()
            )
            it.apply()
        }

        assertThat(musicRepository.shouldPostFavoriteTracks(), IsEqual(true))
    }

    @Test
    fun shouldPostFavoriteArtists_送信間隔内_falseが返却されること() {

        prefData.edit().let {
            it.putString(
                Constants.SP_JIGOKUMIMI_POSTED_FAVORITE_ARTISTS_DATETIME_KEY,
                System.currentTimeMillis().toString()
            )
            it.apply()
        }

        assertThat(musicRepository.shouldPostFavoriteArtists(), IsEqual(false))
    }

    @Test
    fun shouldPostFavoriteArtists_送信間隔外_trueが返却されること() {

        prefData.edit().let {
            it.putString(
                Constants.SP_JIGOKUMIMI_POSTED_FAVORITE_ARTISTS_DATETIME_KEY,
                (System.currentTimeMillis() - Constants.POST_MUSIC_PERIOD - 1).toString()
            )
            it.apply()
        }

        assertThat(musicRepository.shouldPostFavoriteArtists(), IsEqual(true))
    }

    @Test
    fun getTrackDetail_例外発生_削除Trackモデルが返却されること() {

        every { runBlocking { spotifyApiService.getTrackDetail(any(), any()) } } throws Exception()

        val ret = runBlocking {
            musicRepository.getTrackDetail(
                faker.random().hex(),
                faker.number().randomDigit(),
                faker.number().randomDigit()
            )
        }

        assertThat(ret.isDeleted, IsEqual(true))
        assertThat(ret.name, IsEqual(Constants.DELETED_TRACK))
    }

    @Test
    fun getArtistDetail_例外発生_削除Artistモデルが返却されること() {

        every { runBlocking { spotifyApiService.getArtistDetail(any(), any()) } } throws Exception()

        val ret = runBlocking {
            musicRepository.getArtistDetail(
                faker.random().hex(),
                faker.number().randomDigit(),
                faker.number().randomDigit()
            )
        }

        assertThat(ret.isDeleted, IsEqual(true))
        assertThat(ret.name, IsEqual(Constants.DELETED_ARTIST))
    }
}