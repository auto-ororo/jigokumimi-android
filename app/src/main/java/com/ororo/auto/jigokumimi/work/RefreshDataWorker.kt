package com.ororo.auto.jigokumimi.work

import android.app.Application
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ororo.auto.jigokumimi.database.getDatabase
import retrofit2.HttpException


class RefreshDataWorker(application: Application, params: WorkerParameters) :
    CoroutineWorker(application.applicationContext, params) {

    override suspend fun doWork(): Result {
        val database = getDatabase(applicationContext)

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