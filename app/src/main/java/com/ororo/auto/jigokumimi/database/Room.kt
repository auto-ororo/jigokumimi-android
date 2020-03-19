package com.ororo.auto.jigokumimi.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * アクセス先のデータベース、及びDAOを定義
 *
 */

@Dao
interface TrackDao {
    @Transaction
    @Query("select * from tracksaround order by rank")
    fun getTracks(): LiveData<List<TracksAround>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTrack(tracks: List<TracksAround>)
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


@Database(
    entities = [TracksAround::class, DatabaseSpotifyToken::class],
    version = 1
)
abstract class TracksDatabase : RoomDatabase() {
    abstract val trackDao: TrackDao
    abstract val spotifyTokenDao: SpotifyTokenDao
}

private lateinit var INSTANCE: TracksDatabase

fun getDatabase(context: Context): TracksDatabase {
    synchronized(TracksDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                TracksDatabase::class.java,
                "jikomumimi-db"
            ).build()
        }
    }
    return INSTANCE
}