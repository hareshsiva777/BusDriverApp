package com.example.busdriverapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey
    val tripId: String =
        UUID.randomUUID().toString(),

    val driverId: String,
    val routeId: String,
    val startTime: Long,
    val endTime: Long? = null,

    // Odometer entered by the driver before ending the journey.
    val endOdometerKm: Double? = null,

    val status: String = "ACTIVE",
    val isSynced: Boolean = false
)