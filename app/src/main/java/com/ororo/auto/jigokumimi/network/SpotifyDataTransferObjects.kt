package com.ororo.auto.jigokumimi.network

import android.location.Location
import com.ororo.auto.jigokumimi.firebase.MusicAroundItem
import com.ororo.auto.jigokumimi.firebase.PostMusicAroundRequest
import com.ororo.auto.jigokumimi.util.Constants
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * アプリ ー SpotifyAPI間のAPI通信で利用するDTO群を定義
 */

/**
 * Post for [me/tracks/] Request
 */
@JsonClass(generateAdapter = true)
data class SaveTracksRequest(
    val ids: List<String>
)

/**
 * Post for [/me/following] Request
 */
@JsonClass(generateAdapter = true)
data class FollowArtistsOrUsersRequest(
    val ids: List<String>
)

/**
 * Get for [/tracks/{id}] response
 */
@JsonClass(generateAdapter = true)
data class GetTrackDetailResponse(
    val album: SpotifyAlbum,
    val artists: List<SpotifyArtist>,
    @Json(name = "available_markets") val availableMarkets: List<String>,
    @Json(name = "disc_number") val discNumber: Int,
    @Json(name = "duration_ms") val durationMs: Int,
    val explicit: Boolean,
    @Json(name = "external_ids") val externalIds: Map<String, String>,
    @Json(name = "external_urls") val externalUrls: Map<String, String>,
    val href: String,
    val id: String,
    @Json(name = "is_local") val isLocal: Boolean,
    val name: String,
    val popularity: Int,
    @Json(name = "preview_url") val previewUrl: String?,
    @Json(name = "track_number") val trackNumber: Int,
    val type: String,
    val uri: String
)

/**
 * Get for [/me/top/tracks] response
 */
@JsonClass(generateAdapter = true)
data class GetMyFavoriteTracksResponse(
    val items: List<SpotifyTrack>,
    val total: Int,
    val limit: Int,
    val offset: Int,
    val previous: String?,
    val href: String?,
    val next: String?
)

/**
 * Spotifyから取得したお気に入り曲リストを元にJigokumimiへ送信するリクエストBodyを作成する
 */
fun GetMyFavoriteTracksResponse.asPostMusicAroundRequest(
    spotifyUserId: String,
    location: Location
): PostMusicAroundRequest {

    return PostMusicAroundRequest(
        type = Constants.Type.TRACK,
        userId = spotifyUserId,
        location = location,
        musicAroundItems = items.map {
            return@map MusicAroundItem(
                musicItemId = it.id,
                popularity = it.popularity
            )
        }
    )
}

/**
 * Get for [/me/top/artists] response
 */
@JsonClass(generateAdapter = true)
data class GetMyFavoriteArtistsResponse(
    val items: List<SpotifyArtistFull>,
    val total: Int,
    val limit: Int,
    val offset: Int,
    val previous: String?,
    val href: String?,
    val next: String?
)

/**
 * Spotifyから取得したお気に入りアーティストリストを元にJigokumimiへ送信するリクエストBodyを作成する
 */
fun GetMyFavoriteArtistsResponse.asPostMusicAroundRequest(
    spotifyUserId: String,
    location: Location
): PostMusicAroundRequest {

    return PostMusicAroundRequest(
        type = Constants.Type.ARTIST,
        userId = spotifyUserId,
        location = location,
        musicAroundItems = items.map {
            return@map MusicAroundItem(
                musicItemId = it.id,
                popularity = it.popularity
            )
        }
    )
}

/**
 * Spotify song
 */
@JsonClass(generateAdapter = true)
data class SpotifyTrack(
    val album: SpotifyAlbum,
    val artists: List<SpotifyArtist>,
    @Json(name = "available_markets") val availableMarkets: List<String>,
    @Json(name = "disc_number") val discNumber: Int,
    @Json(name = "duration_ms") val durationMs: Int,
    val explicit: Boolean,
    @Json(name = "external_ids") val externalIds: Map<String, String>,
    @Json(name = "external_urls") val externalUrls: Map<String, String>,
    val href: String,
    val id: String,
    @Json(name = "is_local") val isLocal: Boolean,
    val name: String,
    val popularity: Int,
    @Json(name = "preview_url") val previewUrl: String?
)

/**
 * Spotify album
 */
@JsonClass(generateAdapter = true)
data class SpotifyAlbum(
    @Json(name = "album_type") val albumType: String,
    val artists: List<SpotifyArtist>,
    @Json(name = "available_markets") val availableMarkets: List<String>,
    @Json(name = "external_urls") val externalUrls: Map<String, String>,
    val href: String,
    val id: String,
    val images: List<SpotifyImage>,
    val name: String,
    @Json(name = "release_date") val releaseDate: String,
    @Json(name = "release_date_precision") val releaseDatePrecision: String,
    @Json(name = "total_tracks") val totalTracks: Int,
    val type: String,
    val uri: String
)

/**
 * Spotify artist
 */
@JsonClass(generateAdapter = true)
data class SpotifyArtist(
    @Json(name = "external_urls") val externalUrls: Map<String, String>,
    val href: String,
    val id: String,
    val name: String,
    val type: String,
    val uri: String
)

/**
 * Spotify artist
 */
@JsonClass(generateAdapter = true)
data class SpotifyArtistFull(
    @Json(name = "external_urls") val externalUrls: Map<String, String>,
    val followers: SpotifyFollower,
    val genres: List<String>?,
    val href: String,
    val id: String,
    val images: List<SpotifyImage>,
    val name: String,
    val popularity: Int,
    val type: String,
    val uri: String
)

/**
 * Spotify image
 */
@JsonClass(generateAdapter = true)
data class SpotifyImage(
    val height: Int,
    val url: String,
    val width: Int
)

/**
 * Spotify follower
 */
@JsonClass(generateAdapter = true)
data class SpotifyFollower(
    val href: String?,
    val total: Int
)

/**
 * Spotify user
 */
@JsonClass(generateAdapter = true)
data class SpotifyUserResponse(
    val country: String?,
    @Json(name = "display_name") val displayName: String?,
    val email: String,
    @Json(name = "external_urls") val externalUrls: Map<String, String>,
    val followers: SpotifyFollower,
    val href: String,
    val id: String,
    val images: List<SpotifyImage>?,
    val product: String?,
    val type: String,
    val uri: String
)
