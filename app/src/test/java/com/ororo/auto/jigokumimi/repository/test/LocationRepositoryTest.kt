package com.ororo.auto.jigokumimi.repository.test

import android.location.Geocoder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.javafaker.Faker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.ororo.auto.jigokumimi.repository.LocationRepository
import io.mockk.spyk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment.application
import java.util.*

// ※Androidテストで挙動確認を行う

//@RunWith(AndroidJUnit4::class)
//class LocationRepositoryTest {

//}