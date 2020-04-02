package com.ororo.auto.jigokumimi.repository

import android.location.Location
import androidx.lifecycle.LiveData
import com.ororo.auto.jigokumimi.domain.Artist
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
     * ユーザーのお気に入り曲を取得する
     */
    suspend fun getMyFavoriteTracks(): GetMyFavoriteTracksResponse

    /**
     * ユーザーのお気に入り曲を位置情報と一緒に登録する
     */
    suspend fun postMyFavoriteTracks(tracks: List<PostMyFavoriteTracksRequest>): PostResponse

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
    suspend fun postMyFavoriteArtists(artists: List<PostMyFavoriteArtistsRequest>): PostResponse

    /**
     * Spotify上でお気に入り曲を登録/解除する
     */
    suspend fun changeTrackFavoriteState(trackId: String, state: Boolean)

    /**
     * Spotify上のアーティストをフォロー/フォロー解除する
     */
    suspend fun changeArtistFollowState(userId: String, state: Boolean)

}