package com.ororo.auto.jigokumimi.repository

import android.app.Application
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.*

class LocationRepository(application: Application) : ILocationRepository {

    val fusedLocationClient: FusedLocationProviderClient = FusedLocationProviderClient(application)

    val geocoder: Geocoder = Geocoder(application, Locale.getDefault())

    /**
     * 現在の位置情報を取得する
     */
    override fun getCurrentLocation(): Flow<Location> = callbackFlow {
        val request = LocationRequest().also {
            it.interval = 500
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
     * 緯度･経度から地点名を取得する
     */
    override fun getPlaceName(latitude: Double, longitude: Double): String {
        val result = StringBuilder();

        val addresses = geocoder.getFromLocation(latitude, longitude, 1);

        addresses.map {
            result.append(it.adminArea)
            it.subAdminArea?.let {subAdminArea ->
                result.append(subAdminArea)
            }
            result.append(' ')
            result.append(it.locality)
        }

        return result.toString()
    }
}