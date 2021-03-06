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
    val previewUrl: String?,
    val rank: Int,
    val popularity: Int,
    var isSaved: Boolean,
    val isDeleted: Boolean
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
    var isFollowed: Boolean,
    val isDeleted: Boolean
)


/**
 * domain history
 */
data class History(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val place: String,
    val distance: Int,
    val createdAt: String,
    val historyItems: List<HistoryItem>?
)


/**
 * domain history item
 */
data class HistoryItem(
    val rank: Int,
    val popularity: Int,
    val spotifyItemId: String
)
