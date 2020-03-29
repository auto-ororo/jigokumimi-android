package com.ororo.auto.jigokumimi.repository

import android.app.Application
import android.content.SharedPreferences
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.preference.PreferenceManager
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

class MusicRepository(
    private val musicDao: MusicDao,
    private val prefData: SharedPreferences,
    private val spotifyApiService: SpotifyApiService,
    private val jigokumimiApiService: JigokumimiApiService
) : IMusicRepository {

    /**
     * 周辺曲情報
     */
    override val tracks: LiveData<List<Track>> = Transformations.map(musicDao.getTracks()) {
        Timber.d("data changed: $it")
        it.asTrackModel()
    }

    /**
     * 周辺アーティスト情報
     */
    override val artists: LiveData<List<Artist>> = Transformations.map(musicDao.getArtists()) {
        Timber.d("data changed: $it")
        it.asArtistModel()
    }

    val limit: Int = 20
    val offset: Int = 0
    val total: Int = Int.MAX_VALUE

    /**
     * 周辺曲情報を更新する
     */
    override suspend fun refreshTracks(spotifyUserId: String, location: Location, distance: Int) =
        withContext(Dispatchers.IO) {
            Timber.d("refresh tracks is called")

            val jigokumimiToken = prefData.getString(Constants.SP_JIGOKUMIMI_TOKEN_KEY, "")!!
            Timber.d("トークン: $jigokumimiToken")

            // ユーザーのお気に入り曲一覧を取得し､リクエストを作成
            val tracksAround = jigokumimiApiService.getTracksAround(
                authorization = jigokumimiToken,
                userId = spotifyUserId,
                latitude = location.latitude,
                longitude = location.longitude,
                distance = distance
            )

            val databaseTracks = tracksAround.data?.map { trackAround ->
                val spotifyToken = prefData.getString(Constants.SP_SPOTIFY_TOKEN_KEY, "")!!
                val trackDetail = spotifyApiService.getTrackDetail(
                    spotifyToken,
                    trackAround.spotifyTrackId
                )
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
                musicDao.insertTrack(it)
            }

            return@withContext
        }

    /**
     * ユーザーのお気に入り曲を取得する
     */
    override suspend fun getMyFavoriteTracks(): GetMyFavoriteTracksResponse =
        withContext(Dispatchers.IO) {
            Timber.d("get my favorite tracks is called")

            val spotifyToken = prefData.getString(Constants.SP_SPOTIFY_TOKEN_KEY, "")!!

            return@withContext spotifyApiService.getTracks(
                spotifyToken,
                limit,
                offset
            )
        }

    /**
     * ユーザーのお気に入り曲を位置情報と一緒に登録する
     */
    override suspend fun postMyFavoriteTracks(tracks: List<PostMyFavoriteTracksRequest>): PostResponse =
        withContext(Dispatchers.IO) {
            Timber.d("post my favorite tracks is called")

            val jigokumimiToken = prefData.getString(Constants.SP_JIGOKUMIMI_TOKEN_KEY, "")!!

            return@withContext jigokumimiApiService.postTracks(
                jigokumimiToken,
                tracks
            )
        }

    /**
     * 周辺アーティスト情報を更新する
     */
    override suspend fun refreshArtists(spotifyUserId: String, location: Location, distance: Int) =
        withContext(Dispatchers.IO) {
            Timber.d("refresh tracks is called")

            val jigokumimiToken = prefData.getString(Constants.SP_JIGOKUMIMI_TOKEN_KEY, "")!!
            Timber.d("トークン: $jigokumimiToken")


            // ユーザーのお気に入り曲一覧を取得し､リクエストを作成
            val artistsAround = jigokumimiApiService.getArtistsAround(
                authorization = jigokumimiToken,
                userId = spotifyUserId,
                latitude = location.latitude,
                longitude = location.longitude,
                distance = distance
            )

            val databaseArtists = artistsAround.data?.map { artistAround ->
                val spotifyToken = prefData.getString(Constants.SP_SPOTIFY_TOKEN_KEY, "")!!
                val artistDetail = spotifyApiService.getArtistDetail(
                    spotifyToken,
                    artistAround.spotifyArtistId
                )
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
                musicDao.insertArtist(it)
            }
        }

    /**
     * ユーザーのお気に入りアーティストを取得する
     */
    override suspend fun getMyFavoriteArtists(): GetMyFavoriteArtistsResponse =
        withContext(Dispatchers.IO) {
            Timber.d("get my favorite tracks is called")

            val spotifyToken = prefData.getString(Constants.SP_SPOTIFY_TOKEN_KEY, "")!!

            return@withContext spotifyApiService.getArtists(
                spotifyToken,
                limit,
                offset
            )
        }

    /**
     * ユーザーのお気に入りアーティストを更新する
     */
    override suspend fun postMyFavoriteArtists(artists: List<PostMyFavoriteArtistsRequest>): PostResponse =
        withContext(Dispatchers.IO) {
            Timber.d("post my favorite tracks is called")

            val jigokumimiToken = prefData.getString(Constants.SP_JIGOKUMIMI_TOKEN_KEY, "")!!

            return@withContext jigokumimiApiService.postArtists(
                jigokumimiToken,
                artists
            )
        }


    /**
     * Factoryクラス
     */
    companion object {
        @Volatile
        private var INSTANCE: MusicRepository? = null

        fun getRepository(app: Application): MusicRepository {
            return INSTANCE ?: synchronized(this) {

                MusicRepository(
                    getDatabase(app.applicationContext).musicDao,
                    PreferenceManager.getDefaultSharedPreferences(app.applicationContext),
                    SpotifyApi.retrofitService,
                    JigokumimiApi.retrofitService
                ).also {
                    INSTANCE = it
                }
            }
        }
    }
}