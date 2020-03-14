package com.ororo.auto.jigokumimi.repository

import android.app.Activity
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

class LocationRepository(acivity: Activity) {

    val fusedLocationClient: FusedLocationProviderClient = FusedLocationProviderClient(acivity)

    fun getCurrentLocation(): Flow<Location> = callbackFlow {
        val request = LocationRequest().also {
            it.interval = 60000
            it.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                val location = locationResult?.lastLocation ?: return
                offer(location)
                fusedLocationClient.removeLocationUpdates(this)
            }
        }
        fusedLocationClient.requestLocationUpdates(request, callback, null)
        awaitClose {
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }
}