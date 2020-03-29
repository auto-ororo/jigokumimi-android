package com.ororo.auto.jigokumimi.repository

import android.app.Application
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LocationRepository(application: Application) : ILocationRepository {

    val fusedLocationClient: FusedLocationProviderClient = FusedLocationProviderClient(application)

    override fun getCurrentLocation(): Flow<Location> = callbackFlow {
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

    /**
     * Factoryクラス
     */
    companion object {
        @Volatile
        private var INSTANCE: LocationRepository? = null

        fun getRepository(app: Application): LocationRepository {
            return INSTANCE ?: synchronized(this) {

                LocationRepository(
                    app
                ).also {
                    INSTANCE = it
                }
            }
        }
    }
}