package com.ororo.auto.jigokumimi.repository.demo

import android.app.Application
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.domain.Artist
import com.ororo.auto.jigokumimi.domain.History
import com.ororo.auto.jigokumimi.domain.Track
import com.ororo.auto.jigokumimi.network.*
import com.ororo.auto.jigokumimi.repository.IMusicRepository
import com.ororo.auto.jigokumimi.util.demo.CreateDemoDataUtil
import kotlinx.coroutines.delay
import java.util.*

class DemoMusicRepository(
    private val app:Application

) : IMusicRepository {

    val faker = Faker(Locale("jp_JP"))

    val cd = CreateDemoDataUtil(app)

    override val tracks = MutableLiveData<List<Track>>()
    override val artists = MutableLiveData<List<Artist>>()


    override suspend fun refreshTracks(
        spotifyUserId: String,
        location: Location,
        distance: Int
    ): Unit? {
        delay(1000)
        tracks.postValue(cd.createDummyTrackList())

        return null
    }

    override suspend fun refreshTracksFromHistory(history: History): Unit? {
        delay(1000)

        tracks.postValue(cd.createDummyTrackList())

        return null
    }

    override suspend fun refreshArtistsFromHistory(history: History): Unit? {
        delay(1000)

        artists.postValue(cd.createDummyArtistList())

        return null
    }

    override suspend fun getMyFavoriteTracks(): GetMyFavoriteTracksResponse {
        delay(1000)

        return cd.createDummyGetMyFavoriteTracksResponse()
    }

    override suspend fun postMyFavoriteTracks(tracks: List<PostMyFavoriteTracksRequest>): CommonResponse {
        delay(1000)
        return CommonResponse(
            data = null,
            message = faker.lorem().sentence()
        )
    }

    override suspend fun refreshArtists(
        spotifyUserId: String,
        location: Location,
        distance: Int
    ): Unit? {
        delay(1000)

        artists.postValue(cd.createDummyArtistList())
        return null
    }

    override suspend fun getMyFavoriteArtists(): GetMyFavoriteArtistsResponse {
        delay(1000)
        return cd.createDummyGetMyFavoriteArtistsResponse()
    }

    override suspend fun postMyFavoriteArtists(artists: List<PostMyFavoriteArtistsRequest>): CommonResponse {
        delay(1000)
        return CommonResponse(
            data = null,
            message = faker.lorem().sentence()
        )
    }

    override suspend fun changeTrackFavoriteState(trackIndex: Int, state: Boolean) {
    }

    override suspend fun changeArtistFollowState(artistIndex: Int, state: Boolean) {
    }

    override suspend fun getArtistsAroundSearchHistories(userId: String): GetArtistSearchHistoryResponse {
        delay(1000)
        return GetArtistSearchHistoryResponse(
            data = cd.createDummyArtistHistoryList(),
            message = faker.lorem().characters()
        )
    }

    override suspend fun getTracksAroundSearchHistories(userId: String): GetTrackSearchHistoryResponse {
        delay(1000)
        return GetTrackSearchHistoryResponse(
            data = cd.createDummyTrackHistoryList(),
            message = faker.lorem().characters()
        )
    }

    override suspend fun deleteArtistsAroundSearchHistories(userId: String): CommonResponse {
        delay(1000)
        return CommonResponse(
            data = null,
            message = faker.lorem().characters()
        )
    }

    override suspend fun deleteTracksAroundSearchHistories(userId: String): CommonResponse {
        delay(1000)
        return CommonResponse(
            data = null,
            message = faker.lorem().characters()
        )
    }

    override fun shouldPostFavoriteTracks(): Boolean {
        return faker.bool().bool()
    }

    override fun shouldPostFavoriteArtists(): Boolean {
        return faker.bool().bool()
    }
}