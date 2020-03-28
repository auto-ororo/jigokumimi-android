package com.ororo.auto.jigokumimi.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * アクセス先のデータベース、及びDAOを定義
 *
 */

@Dao
interface MusicDao {
    @Transaction
    @Query("select * from trackaround order by rank")
    fun getTracks(): LiveData<List<TrackAround>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTrack(tracks: List<TrackAround>)

    @Transaction
    @Query("select * from artistaround order by rank")
    fun getArtists(): LiveData<List<ArtistAround>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertArtist(artists: List<ArtistAround>)
}

@Database(
    entities = [TrackAround::class, ArtistAround::class],
    version = 1,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {
    abstract val musicDao: MusicDao
}

private lateinit var INSTANCE: MusicDatabase

fun getDatabase(context: Context): MusicDatabase {
    synchronized(MusicDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                MusicDatabase::class.java,
                "jikomumimi-db"
            ).build()
        }
    }
    return INSTANCE
}