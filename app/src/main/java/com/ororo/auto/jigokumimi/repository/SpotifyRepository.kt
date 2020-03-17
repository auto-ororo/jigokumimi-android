package com.ororo.auto.jigokumimi.repository

import com.ororo.auto.jigokumimi.database.DatabaseSpotifyToken
import com.ororo.auto.jigokumimi.database.SongsDatabase
import com.ororo.auto.jigokumimi.network.SpotifyApi
import com.ororo.auto.jigokumimi.network.SpotifyUserResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import timber.log.Timber

class SpotifyRepository(private val database: SongsDatabase) {

    suspend fun refreshSpotifyAuthToken(token: String) =
        withContext(Dispatchers.IO) {
            Timber.d("refresh spotify auth token is called")

            database.spotifyTokenDao.refresh(DatabaseSpotifyToken(token))
        }

    suspend fun getUserProfile(): SpotifyUserResponse  =
        withContext(Dispatchers.IO) {
            Timber.d("get my spotify is called")

            val tmpToken = "Bearer ${database.spotifyTokenDao.getToken()}"
            Timber.d("トークン: $tmpToken")

            return@withContext SpotifyApi.retrofitService.getUserProfile(
                tmpToken
            )
        }
}