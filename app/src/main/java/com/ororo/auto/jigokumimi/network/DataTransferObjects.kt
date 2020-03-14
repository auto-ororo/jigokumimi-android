package com.ororo.auto.jigokumimi.network

import android.provider.ContactsContract
import com.ororo.auto.jigokumimi.database.DatabaseChartInfo
import com.ororo.auto.jigokumimi.database.DatabaseSong
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * アプリーバックエンド間のAPI通信で利用するDTO群を定義
 */

@JsonClass(generateAdapter = true)
data class NetworkSongContainer(
    val items: List<NetworkSong>,
    val total: Int,
    val limit: Int,
    val offset: Int,
    val previous: String?,
    val href: String?,
    val next: String?
)

@JsonClass(generateAdapter = true)
data class PostNetworkSongRequest(
    @Json(name = "spotify_song_id") val spotifySongId: String,
    @Json(name = "spotify_user_id") val spotifyArtistId: String,
    val longitude: Double,
    val latitude: Double,
    val popularity: Int
)

@JsonClass(generateAdapter = true)
data class PostNetworkSongResponse(
    val message: String,
    val data: List<String>?
)

/**
 * network song
 */
@JsonClass(generateAdapter = true)
data class NetworkSong(
    val album: NetworkAlbum,
    val artists: List<NetworkArtist>,
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
 * network album
 */
@JsonClass(generateAdapter = true)
data class NetworkAlbum(
    @Json(name = "album_type") val albumType: String,
    val artists: List<NetworkArtist>,
    @Json(name = "available_markets") val availableMarkets: List<String>,
    @Json(name = "external_urls") val externalUrls: Map<String, String>,
    val href: String,
    val id: String,
    val images: List<NetworkImage>,
    val name: String,
    @Json(name = "release_date") val releaseDate: String,
    @Json(name = "release_date_precision") val releaseDatePrecision: String,
    @Json(name = "total_tracks") val totalTracks: Int,
    val type: String,
    val uri: String
)

/**
 * network artist
 */
@JsonClass(generateAdapter = true)
data class NetworkArtist(
    @Json(name = "external_urls") val externalUrls: Map<String, String>,
    val href: String,
    val id: String,
    val name: String,
    val type: String,
    val uri: String
)

/**
 * network image
 */
@JsonClass(generateAdapter = true)
data class NetworkImage(
    val height: Int,
    val url: String,
    val width: Int
)

/**
 * spotify follower
 */
@JsonClass(generateAdapter = true)
data class SpotifyFollower(
    val href: String?,
    val total: Int
)


/**
 * network spotify user
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
    val images: List<NetworkImage>?,
    val product: String?,
    val type: String,
    val uri: String
)
/**
 * Convert Network results to database objects
 */
fun NetworkSongContainer.asDatabaseSongModel(): List<DatabaseSong> {
    return items.map {
        DatabaseSong(
            id = it.id,
            album = it.album.name,
            name = it.name,
            artist = it.artists[0].name,
            imageUrl = it.album.images[0].url,
            previewUrl = it.previewUrl
        )
    }
}

// TODO 周辺ユーザーの数、再生回数を設定する
fun NetworkSongContainer.asDatabaseChartInfoModel(): List<DatabaseChartInfo> {
    return items.mapIndexed { index, song ->
        DatabaseChartInfo(
            id = 0,
            rank = offset + index + 1,
            songId = song.id,
            playbackUsersCount = 1,
            playbackTimes = 1
        )
    }

}
