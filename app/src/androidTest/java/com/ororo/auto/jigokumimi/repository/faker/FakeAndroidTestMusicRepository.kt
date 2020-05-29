package com.ororo.auto.jigokumimi.repository.faker

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ororo.auto.jigokumimi.domain.Artist
import com.ororo.auto.jigokumimi.domain.History
import com.ororo.auto.jigokumimi.domain.Track
import com.ororo.auto.jigokumimi.network.*
import com.ororo.auto.jigokumimi.repository.IMusicRepository
import java.lang.Exception

class FakeAndroidTestMusicRepository(
    private val _tracks: List<Track> = mutableListOf(),
    private val _artists: List<Artist> = mutableListOf(),
    exception: Exception? = null
) : IMusicRepository, BaseFakeAndroidTestRepository(exception) {

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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun refreshArtistsFromHistory(history: History): Unit? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
        spotifyUserId: String,
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun changeArtistFollowState(artistIndex: Int, state: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getArtistsAroundSearchHistories(userId: String): GetArtistSearchHistoryResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getTracksAroundSearchHistories(userId: String): GetTrackSearchHistoryResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun deleteArtistsAroundSearchHistories(userId: String): CommonResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun deleteTracksAroundSearchHistories(userId: String): CommonResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun shouldPostFavoriteTracks(): Boolean {
        TODO("Not yet implemented")
    }

    override fun shouldPostFavoriteArtists(): Boolean {
        TODO("Not yet implemented")
    }
}