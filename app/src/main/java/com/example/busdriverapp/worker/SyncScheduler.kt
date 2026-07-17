package com.example.busdriverapp.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Creates and schedules trip synchronization work.
 */
object SyncScheduler {

    /**
     * Schedules a one-time synchronization request.
     *
     * The worker waits until the device has a network
     * connection before trying to upload pending trips.
     */
    fun scheduleTripSync(
        context: Context
    ) {
        val networkConstraint =
            Constraints.Builder()
                .setRequiredNetworkType(
                    NetworkType.CONNECTED
                )
                .build()

        val syncRequest =
            OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(networkConstraint)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    10,
                    TimeUnit.SECONDS
                )
                .addTag(
                    SyncWorker.UNIQUE_WORK_NAME
                )
                .build()

        /*
         * KEEP prevents duplicate unfinished sync jobs
         * from being scheduled.
         */
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                SyncWorker.UNIQUE_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                syncRequest
            )
    }
}