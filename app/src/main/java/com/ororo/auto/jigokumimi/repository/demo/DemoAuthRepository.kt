package com.ororo.auto.jigokumimi.repository.demo

import android.app.Application
import com.github.javafaker.Faker
import com.ororo.auto.jigokumimi.network.*
import com.ororo.auto.jigokumimi.repository.IAuthRepository
import com.ororo.auto.jigokumimi.util.demo.CreateDemoDataUtil
import kotlinx.coroutines.delay
import java.util.*

class DemoAuthRepository(
    private val app: Application
) : IAuthRepository {

    val cd = CreateDemoDataUtil(app)

    override fun refreshSpotifyAuthToken(token: String) {
    }

    override suspend fun getSpotifyUserProfile(): SpotifyUserResponse {
        delay(1000)
        return cd.createDummySpotifyUserResponse()
    }

    override suspend fun existsUser(spotifyUserId: String): Boolean {
        return true
    }

    override suspend fun createUser(spotifyUserId: String): String {
        return ""
    }

    override fun getUserId(): String {
        return "userId"
    }
}