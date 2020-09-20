package com.ororo.auto.jigokumimi.repository.demo

import com.ororo.auto.jigokumimi.repository.IAuthRepository

class DemoAuthRepository() : IAuthRepository {

    override fun refreshSpotifyAuthToken(token: String) {
    }

    override suspend fun getSpotifyUserId(): String {
        return "spotifyUserId"
    }

    override suspend fun existsUser(spotifyUserId: String): Boolean {
        return true
    }

    override suspend fun createUser(spotifyUserId: String) {
    }

    override suspend fun getUserId(spotifyUserId: String): String {
        return "userId"
    }
}