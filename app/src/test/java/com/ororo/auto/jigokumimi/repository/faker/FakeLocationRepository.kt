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

class FakeLocationRepository(exception: Exception? = null) : ILocationRepository, BaseFakeRepository(exception) {
    override fun getCurrentLocation(): Flow<Location> = callbackFlow {
        launchExceptionByErrorMode()

        val location = Location("test")
        location.latitude = faker.number().randomDouble(10,2,6)
        location.longitude = faker.number().randomDouble(10,2,6)
        offer(location)
    }
}