package com.ororo.auto.jigokumimi.database

import androidx.room.*
import com.ororo.auto.jigokumimi.domain.Track


/**
 * ローカルDBとの通信で使うEntity群を定義
 */


/**
 * DatabaseSong represents a song entity in the database.
 *
 */

@Entity
data class TracksAround(
    @PrimaryKey
    val id: String,
    val rank: Int,
    val album: String,
    val name: String,
    val artist: String,
    val imageUrl: String,
    val previewUrl: String,
    val popularity: Int
)

/**
 * Map DatabaseSongs to domain entities
 */
fun List<TracksAround>.asDomainModel(): List<Track> {
    return map {
        Track(
            id = it.id,
            album = it.album,
            name = it.name,
            artist = it.artist,
            imageUrl = it.imageUrl,
            previewUrl = it.previewUrl,
            rank = it.rank,
            popularity = it.popularity
        )
    }
}