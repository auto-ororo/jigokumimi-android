package com.ororo.auto.jigokumimi.repository.demo

import android.app.Application
import android.location.Location
import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.repository.ILocationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.*

class DemoLocationRepository(private val app: Application) : ILocationRepository{

    val faker = Faker(Locale("jp_JP"))

    override fun getCurrentLocation(): Flow<Location> = callbackFlow {
        delay(1000)

        val location = Location("test")
        location.latitude = faker.number().randomDouble(10, 2,5)
        location.longitude = faker.number().randomDouble(10, 2,5)
        offer(location)
        awaitClose {
        }
    }

    override fun getPlaceName(latitude: Double, longitude: Double): String {
        val placeList = listOf(
            "東京都墨田区",
            "東京都港区",
            "大阪府大阪市浪速区",
            "福岡県福岡市早良区"
        )

        val index = Random().nextInt(placeList.size)

        return placeList[index]

    }
}