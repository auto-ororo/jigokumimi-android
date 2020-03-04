package com.ororo.auto.jigokumimi.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.ororo.auto.jigokumimi.database.DatabaseSpotifyToken
import com.ororo.auto.jigokumimi.database.SongsDatabase
import com.ororo.auto.jigokumimi.database.asDomainModel
import com.ororo.auto.jigokumimi.domain.Song
import com.ororo.auto.jigokumimi.network.SpotifyApi
import com.ororo.auto.jigokumimi.network.asDatabaseChartInfoModel
import com.ororo.auto.jigokumimi.network.asDatabaseSongModel
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

    var limit: Int = 20

    var offset: Int = 0
    var total: Int = Int.MAX_VALUE

    suspend fun refreshSongs() {
        withContext(Dispatchers.IO) {
            Timber.d("refresh songs is called")

            val tmpToken = "Bearer ${database.spotifyTokenDao.getToken()}"
            Timber.d("トークン: ${tmpToken}")

            val response = SpotifyApi.retrofitService.getTracks(
                tmpToken,
                limit,
                offset
            )
            database.songDao.insertAll(
                response.asDatabaseSongModel(),
                response.asDatabaseChartInfoModel()
            )
        }
    }

    suspend fun refreshSpotifyAuthToken(token: String) {
        withContext(Dispatchers.IO) {
            Timber.d("refresh spotify auth token is called")

            database.spotifyTokenDao.refresh(DatabaseSpotifyToken(token))
        }
    }

}