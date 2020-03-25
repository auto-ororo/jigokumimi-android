package com.ororo.auto.jigokumimi.network

import android.location.Location
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * アプリーバックエンド間のAPI通信で利用するDTO群を定義
 */


/*** Spotify ***/

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
    @Json(name = "preview_url") val previewUrl: String,
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
fun GetMyFavoriteTracksResponse.asPostMyFavoriteTracksRequest(
    spotifyUserId: String,
    location: Location
): List<PostMyFavoriteTracksRequest> {
    return items.map {
        PostMyFavoriteTracksRequest(
            spotifyUserId = spotifyUserId,
            spotifyTrackId = it.id,
            longitude = location.longitude,
            latitude = location.latitude,
            popularity = it.popularity
        )
    }
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
fun GetMyFavoriteArtistsResponse.asPostMyFavoriteArtistsRequest(
    spotifyUserId: String,
    location: Location
): List<PostMyFavoriteArtistsRequest> {
    return items.map {
        PostMyFavoriteArtistsRequest(
            spotifyUserId = spotifyUserId,
            spotifyArtistId = it.id,
            longitude = location.longitude,
            latitude = location.latitude,
            popularity = it.popularity
        )
    }
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
    @Json(name = "preview_url") val previewUrl: String
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

/*** Jigokumimi ***/


/**
 * Post for [auth/create] Request
 */
@JsonClass(generateAdapter = true)
data class SignUpRequest(
    val name: String,
    val email: String,
    val password: String,
    @Json(name = "password_confirmation") val passwordConfirmation: String
)

/**
 * Post for [auth/create] Response
 */
@JsonClass(generateAdapter = true)
data class SignUpResponse(
    val message: String,
    val data: Map<String, String>?
)

/**
 * Post for [auth/login] Request
 */
@JsonClass(generateAdapter = true)
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Post for [auth/login] Response
 */
@JsonClass(generateAdapter = true)
data class LoginResponse(
    val message: String,
    val data: Token
)

/**
 * Post for [auth/login] Response
 */
@JsonClass(generateAdapter = true)
data class LogoutResponse(
    val message: String,
    val data: Map<String, String>?
)

/**
 * Post for [auth/refresh] Response
 */
@JsonClass(generateAdapter = true)
data class RefreshResponse(
    val message: String,
    val data: Token
)


/**
 * Get for [auth/me] Response
 */
@JsonClass(generateAdapter = true)
data class GetMeResponse(
    val message: String,
    val data: JigokumimiUserProfile
)

/**
 * Get for [/tracks] response
 */
@JsonClass(generateAdapter = true)
data class GetTracksAroundResponse(
    val message: String,
    val data: List<TrackAround>?
)

/**
 * Get for [/artists] response
 */
@JsonClass(generateAdapter = true)
data class GetArtistsAroundResponse(
    val message: String,
    val data: List<ArtistAround>?
)

/**
 * Post for [/tracks] request
 */
@JsonClass(generateAdapter = true)
data class PostMyFavoriteTracksRequest(
    @Json(name = "spotify_track_id") val spotifyTrackId: String,
    @Json(name = "spotify_user_id") val spotifyUserId: String,
    val longitude: Double,
    val latitude: Double,
    val popularity: Int
)

/**
 * Post response
 */
@JsonClass(generateAdapter = true)
data class PostResponse(
    val message: String,
    val data: List<String>?
)

/**
 * Post for [/artists] request
 */
@JsonClass(generateAdapter = true)
data class PostMyFavoriteArtistsRequest(
    @Json(name = "spotify_artist_id") val spotifyArtistId: String,
    @Json(name = "spotify_user_id") val spotifyUserId: String,
    val longitude: Double,
    val latitude: Double,
    val popularity: Int
)


/**
 * Track Around
 */
@JsonClass(generateAdapter = true)
data class TrackAround(
    val rank: Int,
    @Json(name = "spotify_track_id") val spotifyTrackId: String,
    val popularity: Int
)

/**
 * Artist Around
 */
@JsonClass(generateAdapter = true)
data class ArtistAround(
    val rank: Int,
    @Json(name = "spotify_artist_id") val spotifyArtistId: String,
    val popularity: Int
)

/**
 * Token Response
 */
@JsonClass(generateAdapter = true)
data class Token(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "token_type") val tokenType: String,
    @Json(name = "expires_in") val expiresIn: Int
)

/**
 * Get for [/me] Response
 */
@JsonClass(generateAdapter = true)
data class JigokumimiUserProfile(
    val id: Int,
    val name: String?,
    val email: String,
    @Json(name = "email_verified_at") val emailVerifiedAt: String?,
    @Json(name = "created_at") val createdAt: String?,
    @Json(name = "updated_at") val updatedAt: String?
)
