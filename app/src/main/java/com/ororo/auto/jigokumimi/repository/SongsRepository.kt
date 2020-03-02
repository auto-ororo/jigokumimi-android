package com.ororo.auto.jigokumimi.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.ororo.auto.jigokumimi.database.DatabaseSpotifyToken
import com.ororo.auto.jigokumimi.database.SongsDatabase
import com.ororo.auto.jigokumimi.database.asDomainModel
import com.ororo.auto.jigokumimi.domain.Song
import com.ororo.auto.jigokumimi.network.SpotifyApi
import com.ororo.auto.jigokumimi.network.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 周辺の曲を検索し、ローカルDBに保存するRepository
 */

class SongsRepository(private val database: SongsDatabase) {

    val songs: LiveData<List<Song>> = Transformations.map(database.songDao.getSongs()) {
        Timber.d("data changed: ${it}")
        it.asDomainModel()
    }

//    val authHeaderToken: LiveData<String> =
//        Transformations.map(database.spotifyTokenDao.getToken()) {
//            Timber.d("token changed: ${it}")
//            "Bearer ${it}"
//        }

    suspend fun refreshSongs() {
        withContext(Dispatchers.IO) {
            Timber.d("refresh songs is called")
            runCatching {
                val tmpToken = "Bearer ${database.spotifyTokenDao.getToken()}"
                Timber.d("トークン: ${tmpToken}")
                SpotifyApi.retrofitService.getTracks(tmpToken)

                // 成功した時(itに戻り値が格納)
            }.onSuccess {
                database.songDao.insertAll(it.asDatabaseModel())
                // 失敗した時(itに例外が格納)
            }.onFailure {
                Timber.e(it.message)
            }

        }
    }

    suspend fun refreshSpotifyAuthToken(token: String) {
        withContext(Dispatchers.IO) {
            Timber.d("refresh spotify auth token is called")

            try {
                database.spotifyTokenDao.refresh(DatabaseSpotifyToken(token))
            } catch (e: Exception) {
                Timber.e(e.message)
            }
        }
    }

}