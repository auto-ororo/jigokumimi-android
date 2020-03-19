package com.ororo.auto.jigokumimi.network

import com.ororo.auto.jigokumimi.database.DatabaseSong
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * アプリーバックエンド間のAPI通信で利用するDTO群を定義
 */


/*** Spotify ***/

/**
 * Get for [/me/tracks] response
 */
@JsonClass(generateAdapter = true)
data class GetMyFavoriteSpotifySongsResponse(
    val items: List<SpotifySong>,
    val total: Int,
    val limit: Int,
    val offset: Int,
    val previous: String?,
    val href: String?,
    val next: String?
)

/**
 * Convert my favorite songs results to database objects
 */
// TODO 周辺ユーザーの数、再生回数を設定する
fun GetMyFavoriteSpotifySongsResponse.asDatabaseSongModel(): List<DatabaseSong> {
    return items.mapIndexed{ index, song ->
        DatabaseSong(
            id = song.id,
            album = song.album.name,
            name = song.name,
            artist = song.artists[0].name,
            imageUrl = song.album.images[0].url,
            previewUrl = song.previewUrl,
            rank = offset + index + 1,
            playbackUsersCount = 1,
            playbackTimes = 1
        )
    }
}

/**
 * Spotify song
 */
@JsonClass(generateAdapter = true)
data class SpotifySong(
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
 * Post for [/songs] request
 */
@JsonClass(generateAdapter = true)
data class PostMyFavoriteSongsRequest(
    @Json(name = "spotify_song_id") val spotifySongId: String,
    @Json(name = "spotify_user_id") val spotifyArtistId: String,
    val longitude: Double,
    val latitude: Double,
    val popularity: Int
)

/**
 * Post for [/songs] response
 */
@JsonClass(generateAdapter = true)
data class PostMyFavoriteSongsResponse(
    val message: String,
    val data: List<String>?
)

