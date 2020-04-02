package com.ororo.auto.jigokumimi.repository

import android.content.SharedPreferences
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.ororo.auto.jigokumimi.database.*
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

    private val limit: Int = 20
    val offset: Int = 0

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

            val tracksDatabase = tracksAround.data?.map { trackAround ->
                val spotifyToken = prefData.getString(Constants.SP_SPOTIFY_TOKEN_KEY, "")!!

                // 詳細な曲情報を取得
                val trackDetail = spotifyApiService.getTrackDetail(
                    spotifyToken,
                    trackAround.spotifyTrackId
                )

                // 曲がお気に入り登録されているかどうかを取得
                val isTrackSaved = spotifyApiService.getIfTracksSaved(
                    spotifyToken,
                    trackAround.spotifyTrackId
                )[0]

                // 取得した情報をエンティティに追加
                return@map DisplayedTrack(

                    id = trackDetail.id,
                    album = trackDetail.album.name,
                    name = trackDetail.name,
                    artists = trackDetail.artists.joinToString(separator = ", ") {
                        it.name
                    },
                    imageUrl = trackDetail.album.images[0].url,
                    previewUrl = trackDetail.previewUrl,
                    rank = trackAround.rank,
                    popularity = trackAround.popularity,
                    isSaved = isTrackSaved
                )
            }

            // ローカルDBに保存
            tracksDatabase?.let {
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

            val artistsDatabase = artistsAround.data?.map { artistAround ->
                val spotifyToken = prefData.getString(Constants.SP_SPOTIFY_TOKEN_KEY, "")!!

                // 詳細アーティスト情報を取得
                val artistDetail = spotifyApiService.getArtistDetail(
                    spotifyToken,
                    artistAround.spotifyArtistId
                )

                val type = "artist"

                // アーティストをフォローしているかどうかを取得
                val isArtistFollowed = spotifyApiService.getIfArtistsOrUsersSaved(
                    spotifyToken,
                    type,
                    artistAround.spotifyArtistId
                )[0]

                // 取得したアーティスト情報を元にエンティティを作成
                return@map DisplayedArtist(
                    id = artistDetail.id,
                    name = artistDetail.name,
                    imageUrl = artistDetail.images[0].url,
                    genres = artistDetail.genres?.joinToString(separator = ", "),
                    rank = artistAround.rank,
                    popularity = artistAround.popularity,
                    isFollowed = isArtistFollowed
                )
            }

            // ローカルDBに保存
            artistsDatabase?.let {
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
     * Spotify上でお気に入り曲を登録/解除する
     */
    override suspend fun changeTrackFavoriteState(trackId: String, state: Boolean) =
        withContext(Dispatchers.IO) {
            Timber.d("change track favorite state called")

            val spotifyToken = prefData.getString(Constants.SP_SPOTIFY_TOKEN_KEY, "")!!

            // フラグに応じてお気に入り登録/解除
            if (state) {
                spotifyApiService.putSaveTracks(
                    spotifyToken,
                    trackId
                )
            } else {
                spotifyApiService.removeSaveTracks(
                    spotifyToken,
                    trackId
                )
            }

            // ローカルDBのフラグ更新
            musicDao.updateTrackSaveFlag(trackId, state)
        }

    /**
     * Spotify上でアーティストをフォロー/フォロー解除する
     */
    override suspend fun changeArtistFollowState(userId: String, state: Boolean) =
        withContext(Dispatchers.IO) {
            Timber.d("change artist follow state called")

            val type = "artist"

            val spotifyToken = prefData.getString(Constants.SP_SPOTIFY_TOKEN_KEY, "")!!

            // フラグに応じてアーティストフォロー/フォロー解除
            if (state) {
                spotifyApiService.followArtistsOrUsers(
                    spotifyToken,
                    type,
                    userId
                )
            } else {
                spotifyApiService.unFollowArtistsOrUsers(
                    spotifyToken,
                    type,
                    userId
                )
            }

            // ローカルDBのフラグ更新
            musicDao.updateArtistSaveFlag(userId, state)
        }
}