package com.example.busdriverapp.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.busdriverapp.data.database.AppDatabase
import com.example.busdriverapp.data.repository.TripRepository

/**
 * Background worker responsible for synchronizing completed trips.
 *
 * WorkManager runs this worker only when the configured
 * network constraint is satisfied.
 */
class SyncWorker(
    appContext: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(
    appContext,
    workerParameters
) {

    /**
     * Performs the background synchronization.
     *
     * Result.success():
     * All pending trips uploaded.
     *
     * Result.retry():
     * Upload failed temporarily and WorkManager should retry.
     */
    override suspend fun doWork(): Result {
        return try {
            Log.d(
                TAG,
                "Trip synchronization started."
            )

            val database =
                AppDatabase.getDatabase(
                    applicationContext
                )

            val repository =
                TripRepository(
                    tripDao = database.tripDao()
                )

            val syncSuccessful =
                repository.syncAllPendingTrips()

            if (syncSuccessful) {
                Log.d(
                    TAG,
                    "All pending trips synchronized."
                )

                Result.success()
            } else {
                Log.w(
                    TAG,
                    "Trip synchronization failed. Retrying."
                )

                Result.retry()
            }
        } catch (exception: Exception) {
            Log.e(
                TAG,
                "Synchronization error.",
                exception
            )

            Result.retry()
        }
    }

    companion object {
        const val UNIQUE_WORK_NAME =
            "completed_trip_sync"

        const val TAG =
            "SyncWorker"
    }
}