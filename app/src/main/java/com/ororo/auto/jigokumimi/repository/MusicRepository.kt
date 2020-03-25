package com.ororo.auto.jigokumimi.repository

import android.content.SharedPreferences
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.ororo.auto.jigokumimi.database.*
import com.ororo.auto.jigokumimi.database.ArtistAround
import com.ororo.auto.jigokumimi.database.TrackAround
import com.ororo.auto.jigokumimi.domain.Artist
import com.ororo.auto.jigokumimi.domain.Track
import com.ororo.auto.jigokumimi.network.*
import com.ororo.auto.jigokumimi.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 周辺の曲を検索し、ローカルDBに保存するRepository
 */

class MusicRepository(private val database: MusicDatabase, private val prefData: SharedPreferences) {

    /**
     * 周辺曲情報
     */
    val tracks: LiveData<List<Track>> = Transformations.map(database.musicDao.getTracks()) {
        Timber.d("data changed: $it")
        it.asTrackModel()
    }

    /**
     * 周辺アーティスト情報
     */
    val artists: LiveData<List<Artist>> = Transformations.map(database.musicDao.getArtists()) {
        Timber.d("data changed: $it")
        it.asArtistModel()
    }

    var limit: Int = 20

    var offset: Int = 0
    var total: Int = Int.MAX_VALUE

    /**
     * 周辺曲情報を更新する
     */
    suspend fun refreshTracks(spotifyUserId: String, location: Location, distance: Int) =
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
                distance =  distance
            )

            val databaseTracks = tracksAround.data?.map { trackAround ->
                val spotifyToken = prefData.getString(Constants.SP_SPOTIFY_TOKEN_KEY,"")!!
                val trackDetail = SpotifyApi.retrofitService.getTrackDetail(spotifyToken, trackAround.spotifyTrackId)
                return@map TrackAround(

                    id = trackDetail.id,
                    album = trackDetail.album.name,
                    name = trackDetail.name,
                    artists = trackDetail.artists.joinToString(separator = ", ") {
                        it.name
                    },
                    imageUrl = trackDetail.album.images[0].url,
                    previewUrl = trackDetail.previewUrl,
                    rank = trackAround.rank,
                    popularity = trackAround.popularity
                )
            }

            databaseTracks?.let {
                database.musicDao.insertTrack(it)
            }
        }

    /**
     * ユーザーのお気に入り曲を取得する
     */
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

    /**
     * ユーザーのお気に入り曲を位置情報と一緒に登録する
     */
    suspend fun postMyFavoriteTracks(tracks: List<PostMyFavoriteTracksRequest>): PostResponse =
        withContext(Dispatchers.IO) {
            Timber.d("post my favorite tracks is called")

            val jigokumimiToken = prefData.getString(Constants.SP_JIGOKUMIMI_TOKEN_KEY,"")!!

            return@withContext JigokumimiApi.retrofitService.postTracks(
                jigokumimiToken,
                tracks
            )
        }

    /**
     * 周辺アーティスト情報を更新する
     */
    suspend fun refreshArtists(spotifyUserId: String, location: Location, distance: Int) =
        withContext(Dispatchers.IO) {
            Timber.d("refresh tracks is called")

            val jigokumimiToken = prefData.getString(Constants.SP_JIGOKUMIMI_TOKEN_KEY,"")!!
            Timber.d("トークン: $jigokumimiToken")


            // ユーザーのお気に入り曲一覧を取得し､リクエストを作成
            val artistsAround = JigokumimiApi.retrofitService.getArtistsAround(
                authorization = jigokumimiToken,
                userId =  spotifyUserId,
                latitude =  location.latitude,
                longitude =  location.longitude,
                distance =  distance
            )

            val databaseArtists = artistsAround.data?.map { artistAround ->
                val spotifyToken = prefData.getString(Constants.SP_SPOTIFY_TOKEN_KEY,"")!!
                val artistDetail = SpotifyApi.retrofitService.getArtistDetail(spotifyToken, artistAround.spotifyArtistId)
                return@map ArtistAround(
                    id = artistDetail.id,
                    name = artistDetail.name,
                    imageUrl = artistDetail.images[0].url,
                    genres = artistDetail.genres?.joinToString(separator = ", "),
                    rank = artistAround.rank,
                    popularity = artistAround.popularity
                )
            }

            databaseArtists?.let {
                database.musicDao.insertArtist(it)
            }
        }

    /**
     * ユーザーのお気に入りアーティストを取得する
     */
    suspend fun getMyFavoriteArtists(): GetMyFavoriteArtistsResponse =
        withContext(Dispatchers.IO) {
            Timber.d("get my favorite tracks is called")

            val spotifyToken = prefData.getString(Constants.SP_SPOTIFY_TOKEN_KEY,"")!!

            return@withContext SpotifyApi.retrofitService.getArtists(
                spotifyToken,
                limit,
                offset
            )
        }

    /**
     * ユーザーのお気に入りアーティストを更新する
     */
    suspend fun postMyFavoriteArtists(artists: List<PostMyFavoriteArtistsRequest>): PostResponse =
        withContext(Dispatchers.IO) {
            Timber.d("post my favorite tracks is called")

            val jigokumimiToken = prefData.getString(Constants.SP_JIGOKUMIMI_TOKEN_KEY,"")!!

            return@withContext JigokumimiApi.retrofitService.postArtists(
                jigokumimiToken,
                artists
            )
        }
}