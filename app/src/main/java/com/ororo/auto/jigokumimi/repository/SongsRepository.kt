package com.ororo.auto.jigokumimi.repository

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.ororo.auto.jigokumimi.database.DatabaseSong
import com.ororo.auto.jigokumimi.database.SongsDatabase
import com.ororo.auto.jigokumimi.database.asDomainModel
import com.ororo.auto.jigokumimi.domain.Song
import com.ororo.auto.jigokumimi.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 周辺の曲を検索し、ローカルDBに保存するRepository
 */

class SongsRepository(private val database: SongsDatabase) {

    val songs: LiveData<List<Song>> = Transformations.map(database.songDao.getSongs()) {
        Timber.d("data changed: $it")
        it.asDomainModel()
    }

    var limit: Int = 20

    var offset: Int = 0
    var total: Int = Int.MAX_VALUE

    suspend fun refreshSongs(spotifyUserId: String, location: Location) =
        withContext(Dispatchers.IO) {
            Timber.d("refresh songs is called")

            val tmpToken = "Bearer ${database.spotifyTokenDao.getToken()}"
            Timber.d("トークン: $tmpToken")


            // ユーザーのお気に入り曲一覧を取得し､リクエストを作成
            val songsAround = getSongsAround(
                spotifyUserId,
                location.latitude,
                location.longitude,
                1000
            )

            val databaseSongSongList = songsAround.data?.map { songAround ->
                val songDetail = getSongDetail(songAround.spotifySongId)
                return@map DatabaseSong(

                    id = songDetail.id,
                    album = songDetail.album.name,
                    name = songDetail.name,
                    artist = songDetail.artists[0].name,
                    imageUrl = songDetail.album.images[0].url,
                    previewUrl = songDetail.previewUrl,
                    rank = songAround.rank,
                    popularity = songAround.popularity
                )

            }

            databaseSongSongList?.let {
                database.songDao.insertSong(it)
            }


        }

    suspend fun getSongsAround(userId: String, latitude: Double, longitude: Double, distance:Int): GetSongsAroundResponse =
        withContext(Dispatchers.IO) {
            Timber.d("get songs around is called")

            return@withContext JigokumimiApi.retrofitService.getSongsAround(
                authorization = null,
                userId =  userId,
                latitude =  latitude,
                longitude =  longitude,
                distance =  distance
            )
        }

    suspend fun getSongDetail(id:String): GetTrackDetailResponse =
        withContext(Dispatchers.IO) {
            Timber.d("get song detail is called")

            val tmpToken = "Bearer ${database.spotifyTokenDao.getToken()}"

            return@withContext SpotifyApi.retrofitService.getTrackDetail(
                tmpToken,
                id
            )
        }

    suspend fun getMyFavoriteSongs(): GetMyFavoriteSongsResponse =
        withContext(Dispatchers.IO) {
            Timber.d("get my favorite songs is called")

            val tmpToken = "Bearer ${database.spotifyTokenDao.getToken()}"

            return@withContext SpotifyApi.retrofitService.getTracks(
                tmpToken,
                limit,
                offset
            )
        }

    suspend fun postMyFavoriteSongs(songs: List<PostMyFavoriteSongsRequest>): PostMyFavoriteSongsResponse =
        withContext(Dispatchers.IO) {
            Timber.d("post my favorite songs is called")

            val tmpToken = "Bearer ${database.spotifyTokenDao.getToken()}"

            return@withContext JigokumimiApi.retrofitService.postSongs(
                tmpToken,
                songs
            )
        }
}