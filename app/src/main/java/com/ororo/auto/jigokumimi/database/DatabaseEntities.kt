package com.ororo.auto.jigokumimi.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ororo.auto.jigokumimi.domain.Song


/**
 * ローカルDBとの通信で使うEntity群を定義
 */


/**
 * DatabaseVideo represents a video entity in the database.
 *
 */
@Entity
data class DatabaseSong constructor(
    @PrimaryKey
    val id: String,
    val album: String,
    val name: String,
    val artist: String,
    val imageUrl: String,
    val previewUrl: String
)

@Entity
data class DatabaseSpotifyToken constructor(
    @PrimaryKey
    val token: String
)

/**
 * Map DatabaseVideos to domain entities
 */
fun List<DatabaseSong>.asDomainModel(): List<Song> {
    return map {
        Song(
            id = it.id,
            album = it.album,
            name = it.name,
            artist = it.artist,
            imageUrl = it.imageUrl,
            previewUrl = it.previewUrl
        )
    }
}