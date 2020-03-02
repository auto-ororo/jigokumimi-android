package com.ororo.auto.jigokumimi.network

import com.ororo.auto.jigokumimi.database.DatabaseSong
import com.ororo.auto.jigokumimi.domain.Song
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * アプリーバックエンド間のAPI通信で利用するDTO群を定義
 *
 * @see domain package for
 */

@JsonClass(generateAdapter = true)
data class NetworkSongContainer(
    val items: List<NetworkSong>,
    val total: Int,
    val limit: Int,
    val offset: Int,
    val previous: Int?,
    val href: String,
    val next: String
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
 * Convert Network results to domain objects
 */
fun NetworkSongContainer.asDomainModel(): List<Song> {
    return items.map {
        Song(
            id = it.id,
            album = it.album.name,
            name = it.name,
            artist = it.artists[0].name,
            imageUrl = it.album.images[0].url,
            previewUrl = it.previewUrl
        )
    }
}

/**
 * Convert Network results to database objects
 */
fun NetworkSongContainer.asDatabaseModel(): List<DatabaseSong> {
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