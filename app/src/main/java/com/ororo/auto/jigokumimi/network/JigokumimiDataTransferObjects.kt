package com.ororo.auto.jigokumimi.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * アプリ ー JigokumimiAPI間のAPI通信で利用するDTO群を定義
 */

/**
 * Post for [auth/create] Request
 */
@JsonClass(generateAdapter = true)
data class SignUpRequest(
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
    val data: JigokumimiToken
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
 * Put for [auth/changePassword] Request
 */
@JsonClass(generateAdapter = true)
data class ChangePasswordRequest(
    @Json(name = "current_password") val currentPassword: String,
    @Json(name = "new_password") val newPassword: String,
    @Json(name = "new_password_confirmation") val newPasswordConfirmation: String
)

/**
 * Post for [auth/refresh] Response
 */
@JsonClass(generateAdapter = true)
data class RefreshResponse(
    val message: String,
    val data: JigokumimiToken
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
    val data: List<TrackAroundNetwork>?
)

/**
 * Get for [/artists] response
 */
@JsonClass(generateAdapter = true)
data class GetArtistsAroundResponse(
    val message: String,
    val data: List<ArtistAroundNetwork>?
)

/**
 * Post for [/tracks] request
 */
@JsonClass(generateAdapter = true)
data class PostMyFavoriteTracksRequest(
    @Json(name = "spotify_track_id") val spotifyTrackId: String,
    @Json(name = "user_id") val userId: String,
    val longitude: Double,
    val latitude: Double,
    val popularity: Int
)

/**
 * Common Jigokumimi response
 */
@JsonClass(generateAdapter = true)
data class CommonResponse(
    val message: String,
    val data: List<String>?
)

/**
 * Post for [/artists] request
 */
@JsonClass(generateAdapter = true)
data class PostMyFavoriteArtistsRequest(
    @Json(name = "spotify_artist_id") val spotifyArtistId: String,
    @Json(name = "user_id") val userId: String,
    val longitude: Double,
    val latitude: Double,
    val popularity: Int
)


/**
 * Get for [/artists/history] Response
 */
@JsonClass(generateAdapter = true)
data class GetArtistSearchHistoryResponse(
    val message: String,
    val data: List<ArtistHistory>?
)


/**
 * Get for [/tracks/history] Response
 */
@JsonClass(generateAdapter = true)
data class GetTrackSearchHistoryResponse(
    val message: String,
    val data: List<TrackHistory>?
)


/**
 * Track History
 */
@JsonClass(generateAdapter = true)
data class TrackHistory(
    val id: String,
    @Json(name = "user_id") val userId: String,
    val latitude: Double,
    val longitude: Double,
    val distance: Int,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "tracks_around_histories") val tracksAroundHistories: List<TrackHistoryItem>?
)

/**
 * Track History Item
 */
@JsonClass(generateAdapter = true)
data class TrackHistoryItem(
    val rank: Int,
    val popularity: Int,
    @Json(name = "spotify_track_id") val spotifyTrackId: String
)


/**
 * Artist History
 */
@JsonClass(generateAdapter = true)
data class ArtistHistory(
    val id: String,
    @Json(name = "user_id") val userId: String,
    val latitude: Double,
    val longitude: Double,
    val distance: Int,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "artists_around_histories") val artistsAroundHistories: List<ArtistHistoryItem>?
)

/**
 * Artist History Item
 */
@JsonClass(generateAdapter = true)
data class ArtistHistoryItem(
    val rank: Int,
    val popularity: Int,
    @Json(name = "spotify_artist_id") val spotifyArtistId: String
)


/**
 * Track Around
 */
@JsonClass(generateAdapter = true)
data class TrackAroundNetwork(
    val rank: Int,
    @Json(name = "spotify_track_id") val spotifyTrackId: String,
    val popularity: Int
)

/**
 * Artist Around
 */
@JsonClass(generateAdapter = true)
data class ArtistAroundNetwork(
    val rank: Int,
    @Json(name = "spotify_artist_id") val spotifyArtistId: String,
    val popularity: Int
)

/**
 * Token Response
 */
@JsonClass(generateAdapter = true)
data class JigokumimiToken(
    val id: String,
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "token_type") val tokenType: String,
    @Json(name = "expires_in") val expiresIn: Int
)

/**
 * Get for [/me] Response
 */
@JsonClass(generateAdapter = true)
data class JigokumimiUserProfile(
    val id: String,
    val name: String?,
    val email: String,
    @Json(name = "email_verified_at") val emailVerifiedAt: String?,
    @Json(name = "created_at") val createdAt: String?,
    @Json(name = "updated_at") val updatedAt: String?
)


