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
    fun getVideos(): LiveData<List<DatabaseSong>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(videos: List<DatabaseSong>)
}

@Database(entities = [DatabaseSong::class], version = 1)
abstract class SongsDatabase : RoomDatabase() {
    abstract val songDao: SongDao
}

private lateinit var INSTANCE: SongsDatabase

fun getDatabase(context: Context): SongsDatabase {
    synchronized(SongsDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                SongsDatabase::class.java,
                "videos"
            ).build()
        }
    }
    return INSTANCE
}