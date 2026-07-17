package com.example.busdriverapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drivers")
data class DriverEntity(
    @PrimaryKey
    val driverId: String,
    val name: String,
    val password: String,
    val lastUpdated: Long = System.currentTimeMillis()
)