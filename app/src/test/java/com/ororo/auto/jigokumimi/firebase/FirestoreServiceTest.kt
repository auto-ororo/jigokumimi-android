package com.ororo.auto.jigokumimi.firebase

import TestCoroutineRule
import android.location.Location
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@RunWith(AndroidJUnit4::class)
class FirestoreServiceTest {

    private var isRunning = true

    lateinit var firestore: FirebaseFirestore
    lateinit var service: FirestoreService

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @Before
    fun setUp() {
        firestore = FirebaseFirestore.getInstance().apply {
            useEmulator(
                "localhost", 8080
            )
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        }
        service = FirestoreService(firestore)

        isRunning = true
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun tracksAround() {

        val latitude = 35.716701
        val longitude = 139.759556

        val tracksAround = listOf(
            MusicAround(
                l = GeoPoint(
                    latitude,
                    longitude
                ),
                userId = "userId1",
                itemId = "1",
                popularity = 1
            ),
            MusicAround(
                l = GeoPoint(
                    latitude + 0.01,
                    longitude + 0.01
                ),
                userId = "userId1",
                itemId = "2",
                popularity = 2
            )
        )

        val location = Location("").apply {
            this.latitude = latitude
            this.longitude = longitude
        }

        lateinit var ret: List<MusicAround>

        GlobalScope.launch {

//            service.postTracksAround(tracksAround)

            ret = service.getMusicAround("aa", location, 0.01)

            isRunning = false
        }

        while (isRunning) {
        }
        assertThat(emptyList(), IsEqual(ret))

        print("aaa")

    }

    @Test
    fun testFoo() = runBlocking {
        foo()
        val a = "aaa"
        Unit
    }

    suspend fun foo(): Unit = suspendCancellableCoroutine { continuation ->
        GlobalScope.launch {
            delay(1000)
            suspendCoroutine<Unit> {
                GlobalScope.launch {
                    delay(1000)
                    continuation.resume(Unit)
                }
            }
        }
    }
}
//class TestCoroutinesRule : TestWatcher() {
//
//   private val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()
//   private val testScope: TestCoroutineScope = TestCoroutineScope(testDispatcher)
//
//   override fun starting(description: Description?) {
//       Dispatchers.setMain(testDispatcher)
//   }
//
//   override fun finished(description: Description?) {
//       Dispatchers.resetMain()
//       testScope.cleanupTestCoroutines()
//   }
//
//   fun runBlockingTest(block: suspend TestCoroutineScope.() -> Unit) {
//       testScope.runBlockingTest(block)
//   }
//}