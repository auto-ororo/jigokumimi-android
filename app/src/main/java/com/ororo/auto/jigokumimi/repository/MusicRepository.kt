package com.ororo.auto.jigokumimi.repository

import android.content.SharedPreferences
import android.location.Location
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.MutableLiveData
import com.ororo.auto.jigokumimi.domain.Artist
import com.ororo.auto.jigokumimi.domain.History
import com.ororo.auto.jigokumimi.domain.Track
import com.ororo.auto.jigokumimi.firebase.*
import com.ororo.auto.jigokumimi.network.GetMyFavoriteArtistsResponse
import com.ororo.auto.jigokumimi.network.GetMyFavoriteTracksResponse
import com.ororo.auto.jigokumimi.network.SpotifyApiService
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
    private val firestoreService: FirestoreService
) : IMusicRepository {

    /**
     * 周辺曲情報
     */
    override val tracks = MutableLiveData<List<Track>>()

    /**
     * 周辺アーティスト情報
     */
    override val artists = MutableLiveData<List<Artist>>()

    val offset: Int = 0

    override suspend fun refreshMusicFromHistory(type: Constants.Type, history: History): Unit? =
        withContext(Dispatchers.IO) {

            return@withContext if (type == Constants.Type.TRACK) {
                history.historyItems?.mapIndexed { index, item ->
                    getTrackDetail(
                        item.spotifyItemId,
                        index.plus(1),
                        item.popularity
                    )
                }.let {
                    tracks.postValue(it)
                }
            } else {
                history.historyItems?.mapIndexed { index, item ->
                    getArtistDetail(
                        item.spotifyItemId,
                        index.plus(1),
                        item.popularity
                    )
                }.let {
                    artists.postValue(it)
                }
            }
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
                Constants.MUSIC_LIST_SIZE,
                offset
            )
        }

    /**
     * 周辺アーティスト情報を更新する
     */
    override suspend fun refreshMusic(
        type: Constants.Type,
        userId: String,
        location: Location,
        place: String,
        distance: Int
    ): Int =
        withContext(Dispatchers.IO) {
            Timber.d("refresh tracks is called")

            // お気に入り曲一覧を取得し､リクエストを作成
            val musicAround = firestoreService.getMusicAroundItems(
                GetMusicAroundItemsRequest(type, userId, location, distance)
            )

            if (musicAround.isEmpty()) return@withContext 0

            firestoreService.postSearchHistory(
                PostSearchHistoryRequest(
                    type,
                    userId,
                    musicAround,
                    location,
                    place,
                    distance
                )
            )

            return@withContext if (type == Constants.Type.TRACK) {
                musicAround.mapIndexed { index, item ->
                    getTrackDetail(
                        item.musicItemId,
                        index.plus(1),
                        item.popularity
                    )
                }.let {
                    tracks.postValue(it)
                    it.size
                }
            } else {
                musicAround.mapIndexed { index, item ->
                    getArtistDetail(
                        item.musicItemId,
                        index.plus(1),
                        item.popularity
                    )
                }.let {
                    artists.postValue(it)
                    it.size
                }
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
                Constants.MUSIC_LIST_SIZE,
                offset
            )
        }

    override suspend fun postMyFavoriteMusic(postMusicAroundRequest: PostMusicAroundRequest) =
        withContext(Dispatchers.IO) {

            firestoreService.postMusicAround(postMusicAroundRequest)

            return@withContext
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
     * 検索履歴を取得する
     */
    override suspend fun getSearchHistories(
        type: Constants.Type,
        userId: String
    ): List<History> =
        withContext(Dispatchers.IO) {
            return@withContext firestoreService.getSearchHistory(
                GetSearchHistoryRequest(
                    type,
                    userId
                )
            ).map { return@map it.convertToHistory() }
        }

    override suspend fun deleteSearchHistory(
        type: Constants.Type,
        userId: String,
        searchHistoryId: String
    ) =
        withContext(Dispatchers.IO) {

            firestoreService.deleteSearchHistory(
                DeleteSearchHistoryRequest(
                    type,
                    userId,
                    searchHistoryId
                )
            )
            return@withContext
        }
}