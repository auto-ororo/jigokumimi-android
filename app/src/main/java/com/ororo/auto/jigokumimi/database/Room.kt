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
    @Query("select * from displayedtrack order by rank")
    fun getTracks(): LiveData<List<DisplayedTrack>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTrack(displayedTrackLogs: List<DisplayedTrack>)

    // 条件でUpdate
    @Query("UPDATE displayedtrack SET isSaved = :isSaved WHERE id = :id")
    fun updateTrackSaveFlag(id: String, isSaved: Boolean)

    @Transaction
    @Query("select * from displayedartist order by rank")
    fun getArtists(): LiveData<List<DisplayedArtist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertArtist(displayedArtist: List<DisplayedArtist>)

    // 条件でUpdate
    @Query("UPDATE displayedartist SET isFollowed = :isFollowed WHERE id = :id")
    fun updateArtistSaveFlag(id: String, isFollowed: Boolean)
}

@Database(
    entities = [DisplayedTrack::class, DisplayedArtist::class],
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
                "jikomumimi"
            ).build()
        }
    }
    return INSTANCE
}