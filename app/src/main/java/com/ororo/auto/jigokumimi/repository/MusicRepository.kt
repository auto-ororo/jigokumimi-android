package com.ororo.auto.jigokumimi.repository

import android.content.SharedPreferences
import android.location.Location
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.MutableLiveData
import com.ororo.auto.jigokumimi.domain.Artist
import com.ororo.auto.jigokumimi.domain.History
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
    private val prefData: SharedPreferences,
    private val spotifyApiService: SpotifyApiService,
    private val jigokumimiApiService: JigokumimiApiService
) : IMusicRepository {

    /**
     * 周辺曲情報
     */
    override val tracks = MutableLiveData<List<Track>>()

    /**
     * 周辺アーティスト情報
     */
    override val artists = MutableLiveData<List<Artist>>()

    private val limit: Int = 20
    val offset: Int = 0

    /**
     * 曲情報を更新する
     */
    override suspend fun refreshTracks(userId: String, location: Location, distance: Int) =
        withContext(Dispatchers.IO) {
            Timber.d("refresh tracks is called")

            val jigokumimiToken = prefData.getString(Constants.SP_JIGOKUMIMI_TOKEN_KEY, "")!!
            Timber.d("トークン: $jigokumimiToken")

            // 周辺のお気に入り曲一覧を取得し､リクエストを作成
            val tracksAround = jigokumimiApiService.getTracksAround(
                authorization = jigokumimiToken,
                userId = userId,
                latitude = location.latitude,
                longitude = location.longitude,
                distance = distance
            )

            // 周辺のお気に入り曲の詳細情報を取得する
            val tracksModel = tracksAround.data?.map { trackAround ->
                getTrackDetail(trackAround.spotifyTrackId, trackAround.rank, trackAround.popularity)
            }

            tracks.postValue(tracksModel)

            return@withContext
        }

    /**
     * 検索履歴から曲情報を更新する
     */
    override suspend fun refreshTracksFromHistory(history: History): Unit? =
        withContext(Dispatchers.IO) {

            // 曲の詳細情報を取得する
            val tracksModel = history.historyItems?.map { historyItem ->
                getTrackDetail(historyItem.spotifyItemId, historyItem.rank, historyItem.popularity)
            }

            tracks.postValue(tracksModel)

            return@withContext
        }

    /**
     * 検索履歴からアーティスト情報を更新する
     */
    override suspend fun refreshArtistsFromHistory(history: History): Unit? =
        withContext(Dispatchers.IO) {

            // アーティストの詳細情報を取得する
            val artistsModel = history.historyItems?.map { historyItem ->
                getArtistDetail(historyItem.spotifyItemId, historyItem.rank, historyItem.popularity)
            }

            artists.postValue(artistsModel)

            return@withContext
        }

    /**
     * Jigokumimi APIから取得した周辺曲情報を元に詳細な曲情報を取得する
     * 詳細情報が見つからなかった場合(Spotify APIで例外が発生した場合)削除Trackモデルを返却する
     *
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    suspend fun getTrackDetail(spotifyTrackId: String, rank: Int, popularity: Int): Track =
        withContext(Dispatchers.IO) {
            val spotifyToken = prefData.getString(Constants.SP_SPOTIFY_TOKEN_KEY, "")!!

            try {
                // 詳細な曲情報を取得
                val trackDetail = spotifyApiService.getTrackDetail(
                    spotifyToken,
                    spotifyTrackId
                )

                // 曲がお気に入り登録されているかどうかを取得
                val isTrackSaved = spotifyApiService.getIfTracksSaved(
                    spotifyToken,
                    spotifyTrackId
                )[0]

                // 取得した情報をエンティティに追加
                return@withContext Track(

                    id = trackDetail.id,
                    album = trackDetail.album.name,
                    name = trackDetail.name,
                    artists = trackDetail.artists.joinToString(separator = ", ") {
                        it.name
                    },
                    imageUrl = trackDetail.album.images[0].url,
                    previewUrl = trackDetail.previewUrl,
                    rank = rank,
                    popularity = popularity,
                    isSaved = isTrackSaved,
                    isDeleted = false
                )

            } catch (e: Exception) {

                return@withContext Track(
                    id = "",
                    isSaved = false,
                    album = "",
                    artists = "",
                    name = Constants.DELETED_TRACK,
                    imageUrl = "",
                    popularity = popularity,
                    rank = rank,
                    previewUrl = null,
                    isDeleted = true
                )
            }


        }


    /**
     * Jigokumimi APIから取得した周辺アーティスト情報を元に詳細なアーティスト情報を取得する
     * 詳細情報が見つからなかった場合(Spotify APIで例外が発生した場合)削除Artistモデルを返却する
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    suspend fun getArtistDetail(
        spotifyArtistId: String,
        rank: Int,
        popularity: Int
    ): Artist =
        withContext(Dispatchers.IO) {
            val spotifyToken = prefData.getString(Constants.SP_SPOTIFY_TOKEN_KEY, "")!!

            try {
                // 詳細なアーティスト情報を取得
                val artistDetail = spotifyApiService.getArtistDetail(
                    spotifyToken,
                    spotifyArtistId
                )

                val type = "artist"

                // アーティストがお気に入り登録されているかどうかを取得
                val isArtistFollowed = spotifyApiService.getIfArtistsOrUsersSaved(
                    spotifyToken,
                    type,
                    spotifyArtistId
                )[0]

                // 取得したアーティスト情報を元にエンティティを作成
                return@withContext Artist(
                    id = artistDetail.id,
                    name = artistDetail.name,
                    imageUrl = artistDetail.images[0].url,
                    genres = artistDetail.genres?.joinToString(separator = ", "),
                    rank = rank,
                    popularity = popularity,
                    isFollowed = isArtistFollowed,
                    isDeleted = false
                )

            } catch (e: Exception) {
                return@withContext Artist(
                    id = spotifyArtistId,
                    name = Constants.DELETED_ARTIST,
                    imageUrl = "",
                    genres = "",
                    rank = rank,
                    popularity = popularity,
                    isFollowed = false,
                    isDeleted = true
                )
            }

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
    override suspend fun postMyFavoriteTracks(tracks: List<PostMyFavoriteTracksRequest>): CommonResponse =
        withContext(Dispatchers.IO) {
            Timber.d("post my favorite tracks is called")

            val jigokumimiToken = prefData.getString(Constants.SP_JIGOKUMIMI_TOKEN_KEY, "")!!

            val response = jigokumimiApiService.postTracks(
                jigokumimiToken,
                tracks
            )

            // 送信日時を保存
            prefData.edit().let {
                it.putString(
                    Constants.SP_JIGOKUMIMI_POSTED_FAVORITE_TRACKS_DATETIME_KEY,
                    System.currentTimeMillis().toString()
                )
                it.apply()
            }

            return@withContext response
        }

    /**
     * 周辺アーティスト情報を更新する
     */
    override suspend fun refreshArtists(userId: String, location: Location, distance: Int) =
        withContext(Dispatchers.IO) {
            Timber.d("refresh tracks is called")

            val jigokumimiToken = prefData.getString(Constants.SP_JIGOKUMIMI_TOKEN_KEY, "")!!
            Timber.d("トークン: $jigokumimiToken")


            // ユーザーのお気に入り曲一覧を取得し､リクエストを作成
            val artistsAround = jigokumimiApiService.getArtistsAround(
                authorization = jigokumimiToken,
                userId = userId,
                latitude = location.latitude,
                longitude = location.longitude,
                distance = distance
            )

            val artistsModel = artistsAround.data?.map { artistAround ->
                getArtistDetail(
                    artistAround.spotifyArtistId,
                    artistAround.rank,
                    artistAround.popularity
                )
            }

            artists.postValue(artistsModel)

            return@withContext
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
    override suspend fun postMyFavoriteArtists(artists: List<PostMyFavoriteArtistsRequest>): CommonResponse =
        withContext(Dispatchers.IO) {
            Timber.d("post my favorite tracks is called")

            val jigokumimiToken = prefData.getString(Constants.SP_JIGOKUMIMI_TOKEN_KEY, "")!!

            val response = jigokumimiApiService.postArtists(
                jigokumimiToken,
                artists
            )

            // 送信日時を保存
            prefData.edit().let {
                it.putString(
                    Constants.SP_JIGOKUMIMI_POSTED_FAVORITE_ARTISTS_DATETIME_KEY,
                    System.currentTimeMillis().toString()
                )
                it.apply()
            }

            return@withContext response
        }

    /**
     * Spotify上でお気に入り曲を登録/解除する
     */
    override suspend fun changeTrackFavoriteState(trackIndex: Int, state: Boolean) =
        withContext(Dispatchers.IO) {
            Timber.d("change track favorite state called")

            tracks.value?.get(trackIndex)?.let {

                val spotifyToken = prefData.getString(Constants.SP_SPOTIFY_TOKEN_KEY, "")!!

                // フラグに応じてお気に入り登録/解除
                if (state) {
                    spotifyApiService.putSaveTracks(
                        spotifyToken,
                        it.id
                    )
                } else {
                    spotifyApiService.removeSaveTracks(
                        spotifyToken,
                        it.id
                    )
                }
            }

            return@withContext
        }

    /**
     * Spotify上でアーティストをフォロー/フォロー解除する
     */
    override suspend fun changeArtistFollowState(artistIndex: Int, state: Boolean) =

        withContext(Dispatchers.IO) {
            Timber.d("change artist follow state called")

            artists.value?.get(artistIndex)?.let { artist ->
                val type = "artist"

                val spotifyToken = prefData.getString(Constants.SP_SPOTIFY_TOKEN_KEY, "")!!

                // フラグに応じてアーティストフォロー/フォロー解除
                if (state) {
                    spotifyApiService.followArtistsOrUsers(
                        spotifyToken,
                        type,
                        artist.id
                    )
                } else {
                    spotifyApiService.unFollowArtistsOrUsers(
                        spotifyToken,
                        type,
                        artist.id
                    )
                }
            }

            return@withContext
        }

    /**
     * アーティスト情報の検索履歴を取得する
     */
    override suspend fun getArtistsAroundSearchHistories(userId: String): GetArtistSearchHistoryResponse =
        withContext(Dispatchers.IO) {
            Timber.d("get artist around search histories is called")

            val jigokumimiToken = prefData.getString(Constants.SP_JIGOKUMIMI_TOKEN_KEY, "")!!

            return@withContext jigokumimiApiService.getArtistsAroundSearchHistories(
                jigokumimiToken,
                userId
            )
        }

    /**
     * 曲情報の検索履歴を取得する
     */
    override suspend fun getTracksAroundSearchHistories(userId: String): GetTrackSearchHistoryResponse =
        withContext(Dispatchers.IO) {
            Timber.d("get artist around search histories is called")

            val jigokumimiToken = prefData.getString(Constants.SP_JIGOKUMIMI_TOKEN_KEY, "")!!

            return@withContext jigokumimiApiService.getTracksAroundSearchHistories(
                jigokumimiToken,
                userId
            )
        }

    /**
     * アーティスト情報の検索履歴を削除する
     */
    override suspend fun deleteArtistsAroundSearchHistories(historyId: String): CommonResponse =
        withContext(Dispatchers.IO) {
            Timber.d("delete artist around search histories is called")

            val jigokumimiToken = prefData.getString(Constants.SP_JIGOKUMIMI_TOKEN_KEY, "")!!

            return@withContext jigokumimiApiService.deleteArtistsAroundSearchHistories(
                jigokumimiToken,
                historyId
            )
        }

    /**
     * 曲情報の検索履歴を削除する
     */
    override suspend fun deleteTracksAroundSearchHistories(historyId: String): CommonResponse =
        withContext(Dispatchers.IO) {
            Timber.d("delete artist around search histories is called")

            val jigokumimiToken = prefData.getString(Constants.SP_JIGOKUMIMI_TOKEN_KEY, "")!!

            return@withContext jigokumimiApiService.deleteTracksAroundSearchHistories(
                jigokumimiToken,
                historyId
            )
        }

    /**
     * 前回の送信日時を元にお気に入り曲を送信すべきかどうかを判断する
     */
    override fun shouldPostFavoriteTracks(): Boolean {

        return shouldPostMusic(Constants.SP_JIGOKUMIMI_POSTED_FAVORITE_TRACKS_DATETIME_KEY)
    }

    /**
     * 前回の送信日時を元にお気に入りアーティストを送信すべきかどうかを判断する
     */
    override fun shouldPostFavoriteArtists(): Boolean {

        return shouldPostMusic(Constants.SP_JIGOKUMIMI_POSTED_FAVORITE_ARTISTS_DATETIME_KEY)
    }

    /**
     * 前回の送信日時を元にお気に入り音楽を送信すべきかどうかを判断する
     */
    private fun shouldPostMusic(sharedPreferencesKey: String): Boolean {

        // 「現在時刻 - 前回の送信日時」が「送信間隔」の外かどうかを返却
        val previousPostedDateTime = prefData.getString(sharedPreferencesKey, "0")!!.toLong()
        val currentDateTime = System.currentTimeMillis()
        return (currentDateTime - previousPostedDateTime) > Constants.POST_MUSIC_PERIOD
    }
}