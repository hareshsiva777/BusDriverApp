package com.example.busdriverapp.location

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.busdriverapp.R
import com.example.busdriverapp.data.database.AppDatabase
import com.example.busdriverapp.data.model.LocationPoint
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch

class LocationTrackingService : LifecycleService() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var activeTripId: String? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val tripId = activeTripId ?: return
            val database = AppDatabase.getDatabase(applicationContext)

            result.locations.forEach { location ->
                lifecycleScope.launch {
                    database.tripDao().insertLocationPoint(
                        LocationPoint(
                            tripId = tripId,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            speed = location.speed,
                            accuracy = location.accuracy,
                            recordedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)

        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START -> {
                activeTripId = intent.getStringExtra(EXTRA_TRIP_ID)

                startAsForegroundService()
                startLocationUpdates()
            }

            ACTION_STOP -> {
                stopLocationTracking()
            }
        }

        return Service.START_NOT_STICKY
    }

    private fun startAsForegroundService() {
        val notification = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
            .setContentTitle("Bus trip in progress")
            .setContentText("Recording the driver's GPS location")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            } else {
                0
            }
        )
    }

    private fun startLocationUpdates() {
        val fineLocationGranted =
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted =
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (!fineLocationGranted && !coarseLocationGranted) {
            stopSelf()
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_INTERVAL_MS
        )
            .setMinUpdateIntervalMillis(
                FASTEST_LOCATION_INTERVAL_MS
            )
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            mainLooper
        )
    }

    private fun stopLocationTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback)

        activeTripId = null

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Trip location tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description =
                    "Shows when a bus trip is recording GPS data."
            }

            val manager = getSystemService(
                NotificationManager::class.java
            )

            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    companion object {
        const val ACTION_START =
            "com.example.busdriverapp.action.START_LOCATION"

        const val ACTION_STOP =
            "com.example.busdriverapp.action.STOP_LOCATION"

        const val EXTRA_TRIP_ID = "extra_trip_id"

        private const val CHANNEL_ID =
            "trip_location_channel"

        private const val NOTIFICATION_ID = 1001

        private const val LOCATION_INTERVAL_MS = 5_000L

        private const val FASTEST_LOCATION_INTERVAL_MS = 3_000L
    }
}