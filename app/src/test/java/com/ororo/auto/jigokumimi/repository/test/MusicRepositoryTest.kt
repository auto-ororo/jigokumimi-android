package com.ororo.auto.jigokumimi.repository.test

import android.content.SharedPreferences
import android.location.Location
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.database.DisplayedArtist
import com.ororo.auto.jigokumimi.database.FakeMusicDao
import com.ororo.auto.jigokumimi.database.DisplayedTrack
import com.ororo.auto.jigokumimi.network.*
import com.ororo.auto.jigokumimi.repository.MusicRepository
import com.ororo.auto.jigokumimi.util.CreateTestDataUtil
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.Test

import org.junit.Before
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class MusicRepositoryTest {
    lateinit var prefData: SharedPreferences
    lateinit var musicDao: FakeMusicDao
    lateinit var jigokumimiApiService: FakeJigokumimiApiService
    lateinit var spotifyApiService: FakeSpotifyApiService
    lateinit var musicRepository: MusicRepository
    lateinit var faker: Faker
    lateinit var ct: CreateTestDataUtil

    @Before
    fun createRepository() {
        prefData =
            PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
        jigokumimiApiService = FakeJigokumimiApiService()
        spotifyApiService = FakeSpotifyApiService()
        musicDao = FakeMusicDao()
        // Get a reference to the class under test
        musicRepository = MusicRepository(
            musicDao,
            prefData,
            spotifyApiService,
            jigokumimiApiService
        )
        faker = Faker(Locale("ja_JP"))
        ct = CreateTestDataUtil()
    }

    @Test
    fun refreshTracks_周辺曲情報と詳細曲情報を元にローカルDBにレコードが保存されること() = runBlocking {
        // テスト対象のメソッドの引数を設定
        val spotifyUserId = faker.random().hex()
        val location = Location("test")
        location.latitude = faker.number().randomDouble(10, 2, 5)
        location.longitude = faker.number().randomDouble(10, 2, 5)
        val distance = faker.number().randomDigit()


        // ダミーデータ、及び期待されるDBレコードを作成
        val tracksAroundNetWork: MutableList<TrackAroundNetwork> = mutableListOf()
        val tracksDetail: MutableList<GetTrackDetailResponse> = mutableListOf()
        val expectedDatabase: MutableList<DisplayedTrack> = mutableListOf()
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
            // メソッド呼び出し後に期待されるDBのレコードを作成
            expectedDatabase.add(
                DisplayedTrack(
                    id = spotifyTrackId,
                    popularity = tracksAroundNetWork.last().popularity,
                    album = tracksDetail.last().album.name,
                    artists = tracksDetail.last().artists.joinToString(separator = ", ") {
                        it.name
                    },
                    imageUrl = tracksDetail.last().album.images.get(0).url,
                    name = tracksDetail.last().name,
                    previewUrl = tracksDetail.last().previewUrl,
                    rank = tracksAroundNetWork.last().rank
                )
            )
        }
        val getTracksAroundResponse = GetTracksAroundResponse(
            data = tracksAroundNetWork,
            message = faker.random().toString()
        )
        jigokumimiApiService.getTracksAroundResponse = getTracksAroundResponse
        spotifyApiService.tracksDetail = tracksDetail
        expectedDatabase.sortBy { it.rank }

        //メソッド呼び出し
        musicRepository.refreshTracks(spotifyUserId, location, distance)

        // ローカルDBを取得
        val actualDatabase = musicDao.getTracks()

        // レスポンスが期待値と等しいことを確認
        actualDatabase.value!!.mapIndexed { i, actualTrackAround ->
            assertThat(actualTrackAround, IsEqual(expectedDatabase.get(i)) )
        }

        return@runBlocking
    }

    @Test
    fun refreshArtists_周辺アーティスト情報と詳細アーティスト情報を元にローカルDBにレコードが保存されること() = runBlocking {
        // テスト対象のメソッドの引数を設定
        val spotifyUserId = faker.random().hex()
        val location = Location("test")
        location.latitude = faker.number().randomDouble(10, 2, 5)
        location.longitude = faker.number().randomDouble(10, 2, 5)
        val distance = faker.number().randomDigit()


        // ダミーデータ、及び期待されるDBレコードを作成
        val artistsAroundNetWork: MutableList<ArtistAroundNetwork> = mutableListOf()
        val artistsDetail: MutableList<SpotifyArtistFull> = mutableListOf()
        val expectedDatabase: MutableList<DisplayedArtist> = mutableListOf()
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
            // メソッド呼び出し後に期待されるDBレコードを作成
            expectedDatabase.add(
                DisplayedArtist(
                    id = spotifyArtistId,
                    popularity = artistsAroundNetWork.last().popularity,
                    imageUrl = artistsDetail.last().images.get(0).url,
                    name = artistsDetail.last().name,
                    rank = artistsAroundNetWork.last().rank,
                    genres = artistsDetail.last().genres?.joinToString(separator = ", ")
                )
            )
        }
        val getArtistsAroundResponse = GetArtistsAroundResponse(
            data = artistsAroundNetWork,
            message = faker.random().toString()
        )
        jigokumimiApiService.getArtistsAroundResponse = getArtistsAroundResponse
        spotifyApiService.artistsDetail = artistsDetail
        expectedDatabase.sortBy { it.rank }

        //メソッド呼び出し
        musicRepository.refreshArtists(spotifyUserId, location, distance)

        // ローカルDBを取得
        val actualDatabase = musicDao.getArtists()

        // レスポンスが期待値と等しいことを確認
        actualDatabase.value!!.mapIndexed { i, actualArtistAround ->
            assertThat(actualArtistAround, IsEqual(expectedDatabase.get(i)) )
        }

        return@runBlocking
    }
}