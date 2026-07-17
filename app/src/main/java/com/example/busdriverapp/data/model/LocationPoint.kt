package com.example.busdriverapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "location_points",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["tripId"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tripId")]
)
data class LocationPoint(
    @PrimaryKey
    val locationId: String = UUID.randomUUID().toString(),
    val tripId: String,
    val latitude: Double,
    val longitude: Double,
    val speed: Float,
    val accuracy: Float,
    val recordedAt: Long = System.currentTimeMillis()
)