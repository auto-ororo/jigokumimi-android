package com.ororo.auto.jigokumimi.repository

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.javafaker.Faker
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class LocationRepositoryTest {

    lateinit var locationRepository: LocationRepository
    lateinit var faker: Faker

    @Before
    fun createRepository() {
        // Get a reference to the class under test
        locationRepository = LocationRepository(ApplicationProvider.getApplicationContext())
        faker = Faker(Locale("ja_JP"))
    }

    @Test
    fun getCurrentLocation() = runBlocking {
        val flow = locationRepository.getCurrentLocation()
        flow.collect {
            assertNotNull(it)
        }

    }
}