package com.ororo.auto.jigokumimi.repository

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface ILocationRepository {
    fun getCurrentLocation(): Flow<Location>

    fun getPlaceName(latitude: Double, longitude: Double): String
}