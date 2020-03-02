package com.ororo.auto.jigokumimi.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * アクセス先のデータベース、及びDAOを定義
 *
 */


@Dao
interface SongDao {
    @Query("select * from databasesong")
    fun getSongs(): LiveData<List<DatabaseSong>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(songs: List<DatabaseSong>)
}

@Dao
interface SpotifyTokenDao {
    @Query("select token from databasespotifytoken")
    fun getToken(): String

    @Query("delete from databasespotifytoken")
    fun deleteAll()

    @Insert()
    fun insert(token: DatabaseSpotifyToken)

    @Transaction
    fun refresh(token: DatabaseSpotifyToken) {
        deleteAll()
        insert(token)
    }
}


@Database(entities = [DatabaseSong::class, DatabaseSpotifyToken::class], version = 1)
abstract class SongsDatabase : RoomDatabase() {
    abstract val songDao: SongDao
    abstract val spotifyTokenDao: SpotifyTokenDao

}

private lateinit var INSTANCE: SongsDatabase

fun getDatabase(context: Context): SongsDatabase {
    synchronized(SongsDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                SongsDatabase::class.java,
                "songs"
            ).build()
        }
    }
    return INSTANCE
}