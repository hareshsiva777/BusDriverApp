package com.example.busdriverapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey
    val routeId: String,
    val routeName: String,
    val origin: String,
    val destination: String,

    // Stop the driver is currently heading towards.
    val nextStop: String = "",

    val scheduledTime: String = "",
    val scheduledTripId: String = "",
    val vehicleNumber: String = "JWR4400",
    val direction: String = "Outbound",
    val lastUpdated: Long = System.currentTimeMillis()
)