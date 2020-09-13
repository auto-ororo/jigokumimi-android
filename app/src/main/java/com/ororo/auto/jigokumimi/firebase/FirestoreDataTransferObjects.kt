package com.ororo.auto.jigokumimi.firebase

import android.location.Location
import android.os.Parcelable
import com.google.firebase.firestore.GeoPoint
import com.ororo.auto.jigokumimi.util.Constants
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.util.*

/**
 * アプリ ー Firestore間のDTO
 */

@Parcelize
data class MusicAround(
    val id: String = "",
    val userId: String = "",
    val musicAroundItems: List<MusicAroundItem> = listOf(),
    val l: @RawValue GeoPoint = GeoPoint(0.0, 0.0), // 位置情報 ※プロパティ名はライブラリ(GeoFirestore)の仕様によるもの
    val g: String = "", // 距離計算に用いるハッシュ値 ※プロパティ名はライブラリ(GeoFirestore)の仕様によるもの
    val createdAt: Date = Date()
) : Parcelable

@Parcelize
data class MusicAroundItem(
    val musicItemId: String = "",
    val popularity: Int = 0
) : Parcelable

@Parcelize
data class PostMusicAroundRequest(
    val type: Constants.Type = Constants.Type.TRACK,
    val userId: String = "",
    val musicAroundItems: List<MusicAroundItem> = listOf(),
    val location: Location = Location(""),
    val createdAt: Date = Date()
) : Parcelable {
    fun convertToMusicAround(id: String) =
        MusicAround(
            id = id,
            userId = userId,
            musicAroundItems = musicAroundItems,
            l = GeoPoint(location.latitude, location.longitude)
        )
}

@Parcelize
data class GetMusicAroundItemsRequest(
    val type: Constants.Type = Constants.Type.TRACK,
    val userId: String = "",
    val location: Location = Location(""),
    val distance: Int = 0
) : Parcelable

@Parcelize
data class SearchHistory(
    val id: String = "",
    val musicAroundItems: List<MusicAroundItem> = listOf(),
    val location: @RawValue GeoPoint = GeoPoint(0.0, 0.0),
    val distance: Int = 0,
    val createdAt: Date = Date()
) : Parcelable

//@Parcelize
//data class HistoryItem(
//    val rank: Int = 0,
//    val musicItemId: String = ""
//) : Parcelable

@Parcelize
data class PostSearchHistoryRequest(
    val type: Constants.Type = Constants.Type.TRACK,
    val userId: String = "",
    val musicAroundItems: List<MusicAroundItem> = listOf(),
    val location: Location = Location(""),
    val distance: Int = 0,
    val createdAt: Date = Date()
) : Parcelable {
    fun convertToSearchHistory(id: String) =
        SearchHistory(
            id = id,
            musicAroundItems = musicAroundItems,
            location = GeoPoint(location.latitude, location.longitude),
            distance = distance,
            createdAt = createdAt
        )
}

@Parcelize
data class GetSearchHistoryRequest(
    val type: Constants.Type = Constants.Type.TRACK,
    val userId: String = ""
) : Parcelable

@Parcelize
data class DeleteSearchHistoryRequest(
    val type: Constants.Type = Constants.Type.TRACK,
    val userId: String = "",
    val searchHistoryId : String = ""
) : Parcelable
