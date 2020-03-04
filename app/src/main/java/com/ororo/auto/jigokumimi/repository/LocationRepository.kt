package com.ororo.auto.jigokumimi.repository

import android.app.Activity
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LocationRepository(acivity: Activity) {

    val fusedLocationClient: FusedLocationProviderClient = FusedLocationProviderClient(acivity)

    //    fun getCurrentLocation(): Flow<Location> = callbackFlow {
//
//        // どのような取得方法を要求
//        val locationRequest = LocationRequest().apply {
//            // 精度重視(電力大)と省電力重視(精度低)を両立するため2種類の更新間隔を指定
//            // 今回は公式のサンプル通りにする。
//            interval = 10000                                   // 最遅の更新間隔(但し正確ではない。)
//            fastestInterval = 5000                             // 最短の更新間隔
//            priority = PRIORITY_HIGH_ACCURACY  // 精度重視
//        }
//
//        // コールバック
//        val locationCallback = object : LocationCallback() {
//            override fun onLocationResult(locationResult: LocationResult?) {
//                // 更新直後の位置が格納されているはず
//                val location = locationResult?.lastLocation ?: return
//                offer(Location(lat = location.latitude, lng = location.longitude))
//            }
//        }
//
//        fusedLocationClient.requestLocationUpdates(
//            locationRequest,
//            locationCallback,
//            null
//        )
//
//        awaitClose {
//            fusedLocationClient.removeLocationUpdates(locationCallback)
//        }
//
//    }
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