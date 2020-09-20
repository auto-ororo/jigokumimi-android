package com.ororo.auto.jigokumimi.repository.faker

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.javafaker.Bool
import com.ororo.auto.jigokumimi.domain.Artist
import com.ororo.auto.jigokumimi.domain.History
import com.ororo.auto.jigokumimi.domain.Track
import com.ororo.auto.jigokumimi.network.*
import com.ororo.auto.jigokumimi.repository.IMusicRepository
import java.lang.Exception

class FakeMusicRepository(
    private val _tracks: List<Track> = mutableListOf(),
    private val _artists: List<Artist> = mutableListOf(),
    exception: Exception? = null,
    private val shouldPostMusic : Boolean = true
) : IMusicRepository, BaseFakeRepository(exception) {

    override val tracks: LiveData<List<Track>>
        get() = MutableLiveData(_tracks)
    override val artists: LiveData<List<Artist>>
        get() = MutableLiveData(_artists)

    override suspend fun refreshTracks(
        spotifyUserId: String,
        location: Location,
        distance: Int
    ): Unit? {
        launchExceptionByErrorMode()
        return null
    }

    override suspend fun refreshTracksFromHistory(history: History): Unit? {
        return Unit
    }

    override suspend fun refreshArtistsFromHistory(history: History): Unit? {
        return Unit
    }

    override suspend fun getMyFavoriteTracks(): GetMyFavoriteTracksResponse {
        launchExceptionByErrorMode()
        return testDataUtil.createDummyGetMyFavoriteTracksResponse()
    }

    override suspend fun postMyFavoriteTracks(tracks: List<PostMyFavoriteTracksRequest>): CommonResponse {
        launchExceptionByErrorMode()
        return CommonResponse(
            data = null,
            message = faker.lorem().sentence()
        )
    }

    override suspend fun refreshArtists(
        userId: String,
        location: Location,
        distance: Int
    ): Unit? {
        launchExceptionByErrorMode()
        return null
    }

    override suspend fun getMyFavoriteArtists(): GetMyFavoriteArtistsResponse {
        return testDataUtil.createDummyGetMyFavoriteArtistsResponse()
    }

    override suspend fun postMyFavoriteArtists(artists: List<PostMyFavoriteArtistsRequest>): CommonResponse {
        launchExceptionByErrorMode()
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
            data = null,
            message = faker.lorem().characters()
        )
    }

    override suspend fun getTracksAroundSearchHistories(userId: String): GetTrackSearchHistoryResponse {
        return GetTrackSearchHistoryResponse(
            data = null,
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
        return shouldPostMusic
    }

    override fun shouldPostFavoriteArtists(): Boolean {
        return shouldPostMusic
    }
}