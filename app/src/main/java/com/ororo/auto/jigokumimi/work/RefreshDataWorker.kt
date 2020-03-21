package com.ororo.auto.jigokumimi.work

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ororo.auto.jigokumimi.database.getDatabase
import com.ororo.auto.jigokumimi.repository.TracksRepository
import retrofit2.HttpException


class RefreshDataWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val database = getDatabase(applicationContext)
        val repository = TracksRepository(database, PreferenceManager.getDefaultSharedPreferences(applicationContext))

        try {
//          repository.refreshTracks()
        } catch (e: HttpException) {
            return Result.retry()
        }
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "com.example.android.devbyteviewer.work.RefreshDataWorker"
    }

}