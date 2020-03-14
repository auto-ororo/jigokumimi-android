package com.ororo.auto.jigokumimi.domain

/**
 *
 * アプリケーション上で表示、計算されるModel群をデータクラスで定義
 */

/**
 * domain song
 */
data class Song(
    val id: String,
    val album: String,
    val name: String,
    val artist: String,
    val imageUrl: String,
    val previewUrl: String,
    val rank: Int,
    val popularity: Int,
    val playbackUsersCount: Int
)