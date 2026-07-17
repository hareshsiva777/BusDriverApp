package com.example.busdriverapp.data.repository

import com.example.busdriverapp.data.api.ApiService
import com.example.busdriverapp.data.dao.TripDao
import com.example.busdriverapp.data.model.LocationPoint
import com.example.busdriverapp.data.model.TripEntity
import kotlinx.coroutines.flow.Flow

class TripRepository(
    private val tripDao: TripDao
) {

    /**
     * Save a newly started trip locally.
     */
    suspend fun startTrip(
        trip: TripEntity
    ) {
        tripDao.insertTrip(trip)
    }

    /**
     * Complete the trip locally.
     *
     * The trip remains unsynchronized until WorkManager
     * successfully uploads it.
     */
    suspend fun endTrip(
        tripId: String,
        endTime: Long,
        endOdometerKm: Double
    ) {
        tripDao.endTrip(
            tripId = tripId,
            endTime = endTime,
            endOdometerKm = endOdometerKm
        )
    }

    /**
     * Save a GPS sample locally.
     */
    suspend fun saveLocation(
        locationPoint: LocationPoint
    ) {
        tripDao.insertLocationPoint(
            locationPoint
        )
    }

    /**
     * Return the active trip when the app is reopened.
     */
    suspend fun getActiveTrip(): TripEntity? {
        return tripDao.getActiveTrip()
    }

    /**
     * Observe GPS points for the active trip.
     */
    fun observeLocationPoints(
        tripId: String
    ): Flow<List<LocationPoint>> {
        return tripDao.observeLocationPoints(
            tripId
        )
    }

    /**
     * Observe the number of GPS points for a trip.
     */
    fun observeLocationPointCount(
        tripId: String
    ): Flow<Int> {
        return tripDao.observeLocationPointCount(
            tripId
        )
    }

    /**
     * Observe completed trips for the history screen.
     */
    fun observeCompletedTrips():
            Flow<List<TripEntity>> {
        return tripDao.observeCompletedTrips()
    }

    /**
     * Get completed trips waiting for synchronization.
     */
    suspend fun getUnsyncedTrips():
            List<TripEntity> {
        return tripDao.getUnsyncedTrips()
    }

    /**
     * Mark a trip as synchronized.
     */
    suspend fun markTripAsSynced(
        tripId: String
    ) {
        tripDao.markTripAsSynced(tripId)
    }

    /**
     * Upload one trip and its GPS samples.
     */
    suspend fun syncTrip(
        trip: TripEntity
    ): Boolean {

        val locationPoints =
            tripDao.getLocationPoints(
                trip.tripId
            )

        val uploaded =
            ApiService.uploadTrip(
                trip = trip,
                locationPoints =
                    locationPoints
            )

        if (uploaded) {
            markTripAsSynced(
                trip.tripId
            )
        }

        return uploaded
    }

    /**
     * Upload every pending completed trip.
     */
    suspend fun syncAllPendingTrips():
            Boolean {

        val pendingTrips =
            getUnsyncedTrips()

        pendingTrips.forEach { trip ->

            val success =
                syncTrip(trip)

            if (!success) {
                return false
            }
        }

        return true
    }
}