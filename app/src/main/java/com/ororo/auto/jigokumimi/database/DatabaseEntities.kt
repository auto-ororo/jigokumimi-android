package com.ororo.auto.jigokumimi.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ororo.auto.jigokumimi.domain.Artist
import com.ororo.auto.jigokumimi.domain.Track


/**
 * ローカルDBとの通信で使うEntity群を定義
 */


/**
 * TrackAround represents a track entity in the database.
 *
 */
@Entity
data class DisplayedTrack(
    @PrimaryKey
    val id: String,
    val rank: Int,
    val album: String,
    val name: String,
    val artists: String,
    val imageUrl: String,
    val previewUrl: String,
    val popularity: Int,
    val isSaved: Boolean
)

/**
 * Map TrackAround  to domain entities
 */
fun List<DisplayedTrack>.asTrackModel(): List<Track> {
    return map {
        Track(
            id = it.id,
            album = it.album,
            name = it.name,
            artists = it.artists,
            imageUrl = it.imageUrl,
            previewUrl = it.previewUrl,
            rank = it.rank,
            popularity = it.popularity,
            isSaved = it.isSaved
        )
    }
}

/**
 * ArtistAround represents a artist entity in the database.
 *
 */
@Entity
data class DisplayedArtist(
    @PrimaryKey
    val id: String,
    val rank: Int,
    val name: String,
    val imageUrl: String,
    val genres: String?,
    val popularity: Int,
    val isFollowed:Boolean
)

/**
 * Map ArtistAround  to domain entities
 */
fun List<DisplayedArtist>.asArtistModel(): List<Artist> {
    return map {
        Artist(
            id = it.id,
            name = it.name,
            genres = it.genres,
            imageUrl = it.imageUrl,
            rank = it.rank,
            popularity = it.popularity,
            isFollowed = it.isFollowed
        )
    }
}
