package com.example.busdriverapp.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.*

class LocationManager(
    context: Context
) {

    private val fusedClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val request = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        5000L
    )
        .setMinUpdateIntervalMillis(3000L)
        .build()

    @SuppressLint("MissingPermission")
    fun startUpdates(callback: LocationCallback) {

        fusedClient.requestLocationUpdates(
            request,
            callback,
            null
        )

    }

    fun stopUpdates(callback: LocationCallback) {

        fusedClient.removeLocationUpdates(callback)

    }

}