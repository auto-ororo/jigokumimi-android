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
        tracks.postValue(cd.createDummyTrackList())

        return null
    }

    override suspend fun refreshTracksFromHistory(history: History): Unit? {

        tracks.postValue(cd.createDummyTrackList())

        return null
    }

    override suspend fun refreshArtistsFromHistory(history: History): Unit? {

        artists.postValue(cd.createDummyArtistList())

        return null
    }

    override suspend fun getMyFavoriteTracks(): GetMyFavoriteTracksResponse {

        return cd.createDummyGetMyFavoriteTracksResponse()
    }

    override suspend fun postMyFavoriteTracks(tracks: List<PostMyFavoriteTracksRequest>): CommonResponse {
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

        artists.postValue(cd.createDummyArtistList())
        return null
    }

    override suspend fun getMyFavoriteArtists(): GetMyFavoriteArtistsResponse {
        return cd.createDummyGetMyFavoriteArtistsResponse()
    }

    override suspend fun postMyFavoriteArtists(artists: List<PostMyFavoriteArtistsRequest>): CommonResponse {
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
        return GetArtistSearchHistoryResponse(
            data = cd.createDummyArtistHistoryList(),
            message = faker.lorem().characters()
        )
    }

    override suspend fun getTracksAroundSearchHistories(userId: String): GetTrackSearchHistoryResponse {
        return GetTrackSearchHistoryResponse(
            data = cd.createDummyTrackHistoryList(),
            message = faker.lorem().characters()
        )
    }

    override suspend fun deleteArtistsAroundSearchHistories(userId: String): CommonResponse {
        return CommonResponse(
            data = null,
            message = faker.lorem().characters()
        )
    }

    override suspend fun deleteTracksAroundSearchHistories(userId: String): CommonResponse {
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