package com.ororo.auto.jigokumimi.domain

/**
 *
 * アプリケーション上で表示、計算されるModel群をデータクラスで定義
 */

/**
 * domain track
 */
data class Track(
    val id: String,
    val album: String,
    val name: String,
    val artists: String,
    val imageUrl: String,
    val previewUrl: String,
    val rank: Int,
    val popularity: Int,
    var isSaved: Boolean
)

/**
 * domain artist
 */
data class Artist(
    val id: String,
    val name: String,
    val imageUrl: String,
    val genres: String?,
    val rank: Int,
    val popularity: Int,
    val isFollowed: Boolean
)
