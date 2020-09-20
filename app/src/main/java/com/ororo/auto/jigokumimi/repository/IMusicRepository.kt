package com.ororo.auto.jigokumimi.repository

import android.location.Location
import androidx.lifecycle.LiveData
import com.ororo.auto.jigokumimi.domain.Artist
import com.ororo.auto.jigokumimi.domain.History
import com.ororo.auto.jigokumimi.domain.Track
import com.ororo.auto.jigokumimi.firebase.PostMusicAroundRequest
import com.ororo.auto.jigokumimi.network.GetMyFavoriteArtistsResponse
import com.ororo.auto.jigokumimi.network.GetMyFavoriteTracksResponse
import com.ororo.auto.jigokumimi.util.Constants

interface IMusicRepository {
    /**
     * 周辺曲情報
     */
    val tracks: LiveData<List<Track>>

    /**
     * 周辺アーティスト情報
     */
    val artists: LiveData<List<Artist>>

    /**
     * 検索履歴から周辺音楽情報を更新する
     */
    suspend fun refreshMusicFromHistory(type: Constants.Type, history: History): Unit?

    /**
     * ユーザーのお気に入り曲を取得する
     */
    suspend fun getMyFavoriteTracks(): GetMyFavoriteTracksResponse

    /**
     * ユーザーのお気に入り音楽を位置情報と一緒に登録する
     */
    suspend fun postMyFavoriteMusic(postMusicAroundRequest: PostMusicAroundRequest)

    /**
     * 周辺音楽情報を更新・検索履歴に追加する
     */
    suspend fun refreshMusic(
        type: Constants.Type,
        userId: String,
        location: Location,
        place: String,
        distance: Int
    ): Int

    /**
     * ユーザーのお気に入りアーティストを取得する
     */
    suspend fun getMyFavoriteArtists(): GetMyFavoriteArtistsResponse

    /**
     * Spotify上でお気に入り曲を登録/解除する
     */
    suspend fun changeTrackFavoriteState(trackIndex: Int, state: Boolean)

    /**
     * Spotify上のアーティストをフォロー/フォロー解除する
     */
    suspend fun changeArtistFollowState(artistIndex: Int, state: Boolean)

    /**
     * 検索履歴を取得する
     */
    suspend fun getSearchHistories(type: Constants.Type, userId: String): List<History>

    /**
     * 検索履歴を削除する
     */
    suspend fun deleteSearchHistory(type: Constants.Type, userId: String, searchHistoryId: String)
}