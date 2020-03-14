package com.ororo.auto.jigokumimi.database

import androidx.room.*
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


@Entity(
    foreignKeys = [ForeignKey( // 外部キー制約
        entity = DatabaseSong::class, // 親のEntityクラスを指定
        parentColumns = arrayOf("id"), // 親Entityの対応するカラム
        childColumns = arrayOf("songId"), // 自分の対応するカラム
        onDelete = ForeignKey.CASCADE // 親EntityがDeleteされたときに子もDelete
    )
    ]
)
data class DatabaseChartInfo constructor(
    @PrimaryKey(autoGenerate = true)
    val id: Int,

    val rank: Int,

    val songId: String,

    val playbackTimes: Int,

    val playbackUsersCount: Int
)


class DatabaseSongWithChartInfo {
    @Embedded
    lateinit var song: DatabaseSong
    @Relation(parentColumn = "id", entityColumn = "songId")
    lateinit var chartInfo: DatabaseChartInfo
}

@Entity
data class DatabaseSpotifyToken constructor(
    @PrimaryKey
    val token: String
)

/**
 * Map DatabaseVideos to domain entities
 */
fun List<DatabaseSongWithChartInfo>.asDomainModel(): List<Song> {
    return map {
        Song(
            id = it.song.id,
            album = it.song.album,
            name = it.song.name,
            artist = it.song.artist,
            imageUrl = it.song.imageUrl,
            previewUrl = it.song.previewUrl,
            rank = it.chartInfo.rank,
            popularity = it.chartInfo.playbackTimes,
            playbackUsersCount = it.chartInfo.playbackUsersCount
        )
    }
}