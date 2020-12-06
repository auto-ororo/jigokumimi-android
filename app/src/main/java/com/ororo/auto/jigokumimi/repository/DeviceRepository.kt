package com.ororo.auto.jigokumimi.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import androidx.core.content.getSystemService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class DeviceRepository(private val context: Context) : IDeviceRepository {

    @ExperimentalCoroutinesApi
    override fun observeNetworkConnection() = callbackFlow<Boolean> {
        val connectivityManager = context.getSystemService<ConnectivityManager>()!!

        val callback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                offer(true)
            }

            override fun onLost(network: Network) {
                offer(false)
            }
        }

        val request = NetworkRequest.Builder().build()

        connectivityManager.registerNetworkCallback(request, callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}