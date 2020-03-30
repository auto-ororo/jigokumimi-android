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

class FakeLocationRepository(
    private val latitude: Double = 0.0,
    private val longitude: Double = 0.0,
    exception: Exception? = null
) : ILocationRepository, BaseFakeRepository(exception) {

    override fun getCurrentLocation(): Flow<Location> = callbackFlow {
        launchExceptionByErrorMode()

        val location = Location("test")
        location.latitude = latitude
        location.longitude = longitude
        offer(location)
        awaitClose {
        }
    }
}