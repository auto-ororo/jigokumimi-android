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

@Database(
    entities = [TracksAround::class],
    version = 1
)
abstract class TracksDatabase : RoomDatabase() {
    abstract val trackDao: TrackDao
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