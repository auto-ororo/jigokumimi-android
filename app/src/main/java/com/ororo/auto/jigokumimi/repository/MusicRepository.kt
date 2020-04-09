package com.ororo.auto.jigokumimi.repository

import android.content.SharedPreferences
import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.ororo.auto.jigokumimi.database.*
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
    private val musicDao: MusicDao,
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

            // ユーザーのお気に入り曲一覧を取得し､リクエストを作成
            val tracksAround = jigokumimiApiService.getTracksAround(
                authorization = jigokumimiToken,
                userId = userId,
                latitude = location.latitude,
                longitude = location.longitude,
                distance = distance
            )

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
            val artistsModel = history.historyItems?.map { historyItem ->
                getArtistDetail(historyItem.spotifyItemId, historyItem.rank, historyItem.popularity)
            }

            artists.postValue(artistsModel)

            return@withContext
    }

    /**
     * Jigokumimi APIから取得した周辺曲情報を元に詳細な曲情報を取得する
     *
     */
    private suspend fun getTrackDetail(spotifyTrackId:String, rank:Int, popularity:Int):Track =
        withContext(Dispatchers.IO) {
            val spotifyToken = prefData.getString(Constants.SP_SPOTIFY_TOKEN_KEY, "")!!

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
                isSaved = isTrackSaved
            )

        }


    /**
     * Jigokumimi APIから取得した周辺アーティスト情報を元に詳細なアーティスト情報を取得する
     *
     */
    private suspend fun getArtistDetail(spotifyArtistId:String, rank:Int, popularity:Int):Artist =
        withContext(Dispatchers.IO) {
            val spotifyToken = prefData.getString(Constants.SP_SPOTIFY_TOKEN_KEY, "")!!

            // 詳細なアーティスト情報を取得
            val artistDetail = spotifyApiService.getArtistDetail(
                spotifyToken,
                spotifyArtistId
            )

            val type = "artist"

            // 曲がお気に入り登録されているかどうかを取得
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
                isFollowed = isArtistFollowed
            )
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

            return@withContext jigokumimiApiService.postTracks(
                jigokumimiToken,
                tracks
            )
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
                return@map Artist(
                    id = artistDetail.id,
                    name = artistDetail.name,
                    imageUrl = artistDetail.images[0].url,
                    genres = artistDetail.genres?.joinToString(separator = ", "),
                    rank = artistAround.rank,
                    popularity = artistAround.popularity,
                    isFollowed = isArtistFollowed
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

            return@withContext jigokumimiApiService.postArtists(
                jigokumimiToken,
                artists
            )
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
                it.isSaved = state
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

                artist.isFollowed = state

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

}