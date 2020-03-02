package com.ororo.auto.jigokumimi.domain

/**
 *
 * アプリケーション上で表示、計算されるModel群をデータクラスで定義
 *
 * @see database for objects that are mapped to the database
 * @see network for objects that parse or prepare network calls
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
    val previewUrl: String
)