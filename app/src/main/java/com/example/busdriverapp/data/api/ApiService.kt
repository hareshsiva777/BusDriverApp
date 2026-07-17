package com.example.busdriverapp.data.api

import android.util.Log
import com.example.busdriverapp.data.model.LocationPoint
import com.example.busdriverapp.data.model.TripEntity
import kotlinx.coroutines.delay

/**
 * Temporary mock API used for the technical assessment.
 *
 * It simulates uploading a completed trip and its GPS points.
 * This can later be replaced with Retrofit and a real backend.
 */
object ApiService {

    private const val TAG = "ApiService"

    suspend fun uploadTrip(
        trip: TripEntity,
        locationPoints: List<LocationPoint>
    ): Boolean {
        return try {

            // Simulate network processing time.
            delay(2_000)

            Log.d(
                TAG,
                """
                Trip uploaded successfully:
                Trip ID: ${trip.tripId}
                Driver ID: ${trip.driverId}
                Route ID: ${trip.routeId}
                Start Time: ${trip.startTime}
                End Time: ${trip.endTime}
                GPS Points: ${locationPoints.size}
                """.trimIndent()
            )

            true

        } catch (exception: Exception) {

            Log.e(
                TAG,
                "Trip upload failed: ${exception.message}",
                exception
            )

            false
        }
    }
}