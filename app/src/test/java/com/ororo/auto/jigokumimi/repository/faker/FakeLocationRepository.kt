package com.ororo.auto.jigokumimi.repository.faker

import android.location.Location
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.ororo.auto.jigokumimi.repository.ILocationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.lang.Exception
import java.util.*

class FakeLocationRepository(
    private val latitude: Double = 0.0,
    private val longitude: Double = 0.0,
    exception: Exception? = null
) : ILocationRepository, BaseFakeRepository(exception) {

    override suspend fun getCurrentLocation(): Location {
        launchExceptionByErrorMode()

        val location = Location("test")
        location.latitude = latitude
        location.longitude = longitude
        return location
    }

    override fun getPlaceName(latitude: Double, longitude: Double): String {
        val placeList = listOf(
            "東京都墨田区押上1-1-2",
            "東京都港区芝公園4-2-8",
            "大阪府大阪市浪速区恵美須東1-18-6",
            "福岡県福岡市早良区百道浜2-3-26"
        )

        val index = Random().nextInt(placeList.size)

        return placeList[index]
    }
}