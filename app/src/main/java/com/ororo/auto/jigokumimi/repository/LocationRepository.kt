package com.ororo.auto.jigokumimi.repository

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.ororo.auto.jigokumimi.R
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.lang.Exception
import java.util.*
import kotlin.coroutines.resume

class LocationRepository(
    private val application: Application
) : ILocationRepository {

    val fusedLocationClient: FusedLocationProviderClient = FusedLocationProviderClient(application)

    val geocoder: Geocoder = Geocoder(application, Locale.getDefault())

    /**
     * 現在の位置情報を取得する
     */
    override suspend fun getCurrentLocation(): Location =
        suspendCancellableCoroutine { continuation ->
            val request = LocationRequest().also {
                it.interval = 500
                it.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            if (ActivityCompat.checkSelfPermission(
                    application,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.requestLocationUpdates(request, object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        super.onLocationResult(locationResult)
                        val location = locationResult?.lastLocation ?: return
                        continuation.resume(location)
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }, null)
            } else {
                throw Throwable(application.applicationContext.getString(R.string.permission_denied_message))
            }
        }

    /**
     * 緯度･経度から地点名を取得する
     */
    override fun getPlaceName(latitude: Double, longitude: Double): String {
        val result = StringBuilder();

        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1);

            addresses.map {
                result.append(it.adminArea)
                it.subAdminArea?.let { subAdminArea ->
                    result.append(subAdminArea)
                }
                result.append(' ')
                result.append(it.locality)
            }

        } catch (e: Exception) {
            Timber.d(e.message)
        }

        return result.toString()
    }
}