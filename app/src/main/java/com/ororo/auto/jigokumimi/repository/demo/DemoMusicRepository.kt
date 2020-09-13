package com.ororo.auto.jigokumimi.repository.demo

import android.app.Application
import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.domain.Artist
import com.ororo.auto.jigokumimi.domain.History
import com.ororo.auto.jigokumimi.domain.Track
import com.ororo.auto.jigokumimi.firebase.PostMusicAroundRequest
import com.ororo.auto.jigokumimi.network.GetMyFavoriteArtistsResponse
import com.ororo.auto.jigokumimi.network.GetMyFavoriteTracksResponse
import com.ororo.auto.jigokumimi.repository.IMusicRepository
import com.ororo.auto.jigokumimi.util.Constants
import com.ororo.auto.jigokumimi.util.demo.CreateDemoDataUtil
import kotlinx.coroutines.delay
import java.util.*

class DemoMusicRepository(
    app: Application
) : IMusicRepository {

    val faker = Faker(Locale("jp_JP"))

    val cd = CreateDemoDataUtil(app)

    override val tracks = MutableLiveData<List<Track>>()
    override val artists = MutableLiveData<List<Artist>>()

    override suspend fun refreshMusic(
        type: Constants.Type,
        userId: String,
        location: Location,
        place: String,
        distance: Int
    ): Int {
        delay(1000)
        if (type == Constants.Type.TRACK) {
            tracks.postValue(cd.createDummyTrackList())
        } else {
            artists.postValue(cd.createDummyArtistList())
        }
        return 10
    }

    override suspend fun refreshMusicFromHistory(type: Constants.Type, history: History): Unit? {
        delay(1000)

        if (type == Constants.Type.TRACK) {
            tracks.postValue(cd.createDummyTrackList())
        } else {
            artists.postValue(cd.createDummyArtistList())
        }
        return null
    }

    override suspend fun getMyFavoriteTracks(): GetMyFavoriteTracksResponse {
        delay(1000)

        return cd.createDummyGetMyFavoriteTracksResponse()
    }

    override suspend fun postMyFavoriteMusic(postMusicAroundRequest: PostMusicAroundRequest) {
        delay(1000)
    }

    override suspend fun getMyFavoriteArtists(): GetMyFavoriteArtistsResponse {
        delay(1000)
        return cd.createDummyGetMyFavoriteArtistsResponse()
    }

    override suspend fun changeTrackFavoriteState(trackIndex: Int, state: Boolean) {}

    override suspend fun changeArtistFollowState(artistIndex: Int, state: Boolean) {}

    override suspend fun getSearchHistories(
        type: Constants.Type,
        userId: String
    ): List<History> {
        return (1..10).map { cd.createDummyHistory() }
    }

    override suspend fun deleteSearchHistory(
        type: Constants.Type,
        userId: String,
        searchHistoryId: String
    ) {
    }

    override fun shouldPostFavoriteMusic(type: Constants.Type): Boolean {
        return true
    }
}