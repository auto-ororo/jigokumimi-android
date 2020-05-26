package com.ororo.auto.jigokumimi.repository

import android.location.Location
import androidx.lifecycle.LiveData
import com.ororo.auto.jigokumimi.domain.Artist
import com.ororo.auto.jigokumimi.domain.History
import com.ororo.auto.jigokumimi.domain.Track
import com.ororo.auto.jigokumimi.network.*

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
     * 周辺曲情報を更新する
     */
    suspend fun refreshTracks(spotifyUserId: String, location: Location, distance: Int): Unit?

    /**
     * 検索履歴から周辺曲情報を更新する
     */
    suspend fun refreshTracksFromHistory(history: History): Unit?

    /**
     * 検索履歴から周辺アーティスト情報を更新する
     */
    suspend fun refreshArtistsFromHistory(history: History): Unit?

    /**
     * ユーザーのお気に入り曲を取得する
     */
    suspend fun getMyFavoriteTracks(): GetMyFavoriteTracksResponse

    /**
     * ユーザーのお気に入り曲を位置情報と一緒に登録する
     */
    suspend fun postMyFavoriteTracks(tracks: List<PostMyFavoriteTracksRequest>): CommonResponse

    /**
     * 周辺アーティスト情報を更新する
     */
    suspend fun refreshArtists(spotifyUserId: String, location: Location, distance: Int): Unit?

    /**
     * ユーザーのお気に入りアーティストを取得する
     */
    suspend fun getMyFavoriteArtists(): GetMyFavoriteArtistsResponse

    /**
     * ユーザーのお気に入りアーティストを更新する
     */
    suspend fun postMyFavoriteArtists(artists: List<PostMyFavoriteArtistsRequest>): CommonResponse

    /**
     * Spotify上でお気に入り曲を登録/解除する
     */
    suspend fun changeTrackFavoriteState(trackIndex: Int, state: Boolean)

    /**
     * Spotify上のアーティストをフォロー/フォロー解除する
     */
    suspend fun changeArtistFollowState(artistIndex: Int, state: Boolean)

    /**
     * アーティスト情報の検索履歴を取得する
     */
    suspend fun getArtistsAroundSearchHistories(userId: String) : GetArtistSearchHistoryResponse

    /**
     * 曲情報の検索履歴を取得する
     */
    suspend fun getTracksAroundSearchHistories(userId: String) : GetTrackSearchHistoryResponse

    /**
     * アーティスト情報の検索履歴を削除する
     */
    suspend fun deleteArtistsAroundSearchHistories(userId: String) : CommonResponse

    /**
     * 曲情報の検索履歴を削除する
     */
    suspend fun deleteTracksAroundSearchHistories(userId: String) : CommonResponse

    /**
     * 前回の送信日時を元にお気に入り曲を送信すべきかどうかを判断する
     */
    fun shouldPostFavoriteTracks() :Boolean

    /**
     * 前回の送信日時を元にお気に入りアーティストを送信すべきかどうかを判断する
     */
    fun shouldPostFavoriteArtists() :Boolean

}