package com.ororo.auto.jigokumimi.firebase

import android.location.Location
import android.os.Parcelable
import com.google.firebase.firestore.GeoPoint
import com.ororo.auto.jigokumimi.domain.History
import com.ororo.auto.jigokumimi.domain.HistoryItem
import com.ororo.auto.jigokumimi.util.Constants
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import org.imperiumlabs.geofirestore.core.GeoHash
import java.util.*

/**
 * アプリ ー Firestore間のDTO
 */

@Parcelize
data class MusicAround(
    val userId: String = "",
    val musicAroundItems: List<MusicAroundItem> = listOf(),
    val l: @RawValue GeoPoint = GeoPoint(0.0, 0.0),
    val g: String = "", // 距離計算に用いるハッシュ値
    val createdAt: Date = Date()
) : Parcelable

@Parcelize
data class MusicAroundItem(
    val musicItemId: String = "",
    val popularity: Int = 0
) : Parcelable

@Parcelize
data class User(
    val id: String = "",
    val spotify: Spotify? = null
) : Parcelable {

    @Parcelize
    data class Spotify(val id: String) : Parcelable
}

@Parcelize
data class PostMusicAroundRequest(
    val type: Constants.Type = Constants.Type.TRACK,
    val userId: String = "",
    val musicAroundItems: List<MusicAroundItem> = listOf(),
    val location: Location = Location(""),
    val createdAt: Date = Date()
) : Parcelable {
    fun convertToMusicAround() =
        MusicAround(
            userId = userId,
            musicAroundItems = musicAroundItems,
            l = GeoPoint(location.latitude, location.longitude),
            g = GeoHash(
                latitude = location.latitude,
                longitude = location.longitude,
                precision = 12
            ).geoHashString
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
    val place: String = "",
    val distance: Int = 0,
    val createdAt: Date = Date()
) : Parcelable {
    fun convertToHistory(): History = History(
        id = this.id,
        longitude = this.location.longitude,
        latitude = this.location.latitude,
        distance = this.distance,
        createdAt = this.createdAt.toString(),
        place = this.place,
        historyItems = this.musicAroundItems.mapIndexed { index, item ->
            return@mapIndexed HistoryItem(
                rank = index.plus(1),
                popularity = item.popularity,
                spotifyItemId = item.musicItemId
            )
        }
    )

}

@Parcelize
data class PostSearchHistoryRequest(
    val type: Constants.Type = Constants.Type.TRACK,
    val userId: String = "",
    val musicAroundItems: List<MusicAroundItem> = listOf(),
    val location: Location = Location(""),
    val place: String = "",
    val distance: Int = 0,
    val createdAt: Date = Date()
) : Parcelable {
    fun convertToSearchHistory(id: String) =
        SearchHistory(
            id = id,
            musicAroundItems = musicAroundItems,
            location = GeoPoint(location.latitude, location.longitude),
            place = place,
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
    val searchHistoryId: String = ""
) : Parcelable

@Parcelize
data class UserRequest(
    val spotifyUserId: String = ""
) : Parcelable
