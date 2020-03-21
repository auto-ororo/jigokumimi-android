package com.ororo.auto.jigokumimi.repository

import android.content.SharedPreferences
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.ororo.auto.jigokumimi.database.TracksAround
import com.ororo.auto.jigokumimi.database.TracksDatabase
import com.ororo.auto.jigokumimi.database.asDomainModel
import com.ororo.auto.jigokumimi.domain.Track
import com.ororo.auto.jigokumimi.network.*
import com.ororo.auto.jigokumimi.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 周辺の曲を検索し、ローカルDBに保存するRepository
 */

class TracksRepository(private val database: TracksDatabase, private val prefData: SharedPreferences) {

    val tracks: LiveData<List<Track>> = Transformations.map(database.trackDao.getTracks()) {
        Timber.d("data changed: $it")
        it.asDomainModel()
    }

    var limit: Int = 20

    var offset: Int = 0
    var total: Int = Int.MAX_VALUE

    suspend fun refreshTracks(spotifyUserId: String, location: Location) =
        withContext(Dispatchers.IO) {
            Timber.d("refresh tracks is called")

            val jigokumimiToken = prefData.getString(Constants.SP_JIGOKUMIMI_TOKEN_KEY,"")!!
            Timber.d("トークン: $jigokumimiToken")


            // ユーザーのお気に入り曲一覧を取得し､リクエストを作成
            val tracksAround = JigokumimiApi.retrofitService.getTracksAround(
                authorization = jigokumimiToken,
                userId =  spotifyUserId,
                latitude =  location.latitude,
                longitude =  location.longitude,
                distance =  1000
            )

            val databaseTracks = tracksAround.data?.map { trackAround ->
                val spotifyToken = prefData.getString(Constants.SP_SPOTIFY_TOKEN_KEY,"")!!
                val trackDetail = SpotifyApi.retrofitService.getTrackDetail(spotifyToken, trackAround.spotifyTrackId)
                return@map TracksAround(

                    id = trackDetail.id,
                    album = trackDetail.album.name,
                    name = trackDetail.name,
                    artist = trackDetail.artists[0].name,
                    imageUrl = trackDetail.album.images[0].url,
                    previewUrl = trackDetail.previewUrl,
                    rank = trackAround.rank,
                    popularity = trackAround.popularity
                )
            }

            databaseTracks?.let {
                database.trackDao.insertTrack(it)
            }
        }

    suspend fun getMyFavoriteTracks(): GetMyFavoriteTracksResponse =
        withContext(Dispatchers.IO) {
            Timber.d("get my favorite tracks is called")

            val spotifyToken = prefData.getString(Constants.SP_SPOTIFY_TOKEN_KEY,"")!!

            return@withContext SpotifyApi.retrofitService.getTracks(
                spotifyToken,
                limit,
                offset
            )
        }

    suspend fun postMyFavoriteTracks(tracks: List<PostMyFavoriteTracksRequest>): PostMyFavoriteTracksResponse =
        withContext(Dispatchers.IO) {
            Timber.d("post my favorite tracks is called")

            val jigokumimiToken = prefData.getString(Constants.SP_JIGOKUMIMI_TOKEN_KEY,"")!!

            return@withContext JigokumimiApi.retrofitService.postTracks(
                jigokumimiToken,
                tracks
            )
        }
}