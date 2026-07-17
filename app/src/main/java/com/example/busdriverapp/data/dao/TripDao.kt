package com.example.busdriverapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.busdriverapp.data.model.LocationPoint
import com.example.busdriverapp.data.model.TripEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(
        trip: TripEntity
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationPoint(
        locationPoint: LocationPoint
    )

    @Query(
        """
        SELECT * FROM trips
        WHERE status = 'ACTIVE'
        ORDER BY startTime DESC
        LIMIT 1
        """
    )
    suspend fun getActiveTrip(): TripEntity?

    @Query(
        """
    UPDATE trips
    SET endTime = :endTime,
        endOdometerKm = :endOdometerKm,
        status = 'COMPLETED',
        isSynced = 0
    WHERE tripId = :tripId
    """
    )
    suspend fun endTrip(
        tripId: String,
        endTime: Long,
        endOdometerKm: Double
    )
    @Query(
        """
        SELECT * FROM trips
        WHERE status = 'COMPLETED'
        AND isSynced = 0
        ORDER BY startTime ASC
        """
    )
    suspend fun getUnsyncedTrips(): List<TripEntity>

    @Query(
        """
        UPDATE trips
        SET isSynced = 1
        WHERE tripId = :tripId
        """
    )
    suspend fun markTripAsSynced(
        tripId: String
    )

    @Query(
        """
        SELECT * FROM trips
        WHERE status = 'COMPLETED'
        ORDER BY startTime DESC
        """
    )
    fun observeCompletedTrips():
            Flow<List<TripEntity>>

    @Query(
        """
        SELECT * FROM location_points
        WHERE tripId = :tripId
        ORDER BY recordedAt ASC
        """
    )
    fun observeLocationPoints(
        tripId: String
    ): Flow<List<LocationPoint>>

    @Query(
        """
        SELECT * FROM location_points
        WHERE tripId = :tripId
        ORDER BY recordedAt ASC
        """
    )
    suspend fun getLocationPoints(
        tripId: String
    ): List<LocationPoint>

    @Query(
        """
        SELECT COUNT(*) FROM location_points
        WHERE tripId = :tripId
        """
    )
    fun observeLocationPointCount(
        tripId: String
    ): Flow<Int>
}