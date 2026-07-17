package com.example.busdriverapp.viewmodel

import android.app.Application
import android.content.Context
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.busdriverapp.data.database.AppDatabase
import com.example.busdriverapp.data.model.RouteEntity
import com.example.busdriverapp.data.model.TripEntity
import com.example.busdriverapp.data.repository.RouteRepository
import com.example.busdriverapp.data.repository.TripRepository
import com.example.busdriverapp.worker.SyncScheduler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Contains all information currently displayed by the dashboard
 * and active-trip screens.
 */
data class DashboardUiState(
    val routes: List<RouteEntity> = emptyList(),
    val selectedRouteId: String? = null,
    val activeTrip: TripEntity? = null,

    // Completed trips stored in Room.
    val completedTrips: List<TripEntity> = emptyList(),

    // Live active-trip information.
    val locationPointCount: Int = 0,
    val currentSpeedKmh: Float = 0f,
    val mileageKm: Double = 0.0,
    val gpsConnected: Boolean = false,

    // General loading state used when local routes are first loaded.
    val isLoading: Boolean = true,

    // Separate loading state for the online route refresh button.
    val isRefreshingRoutes: Boolean = false,

    val message: String? = null
)

/**
 * Manages route selection, active trips, GPS metrics,
 * route refreshing and trip synchronization.
 */
class DashboardViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database =
        AppDatabase.getDatabase(application)

    private val routeRepository =
        RouteRepository(
            routeDao = database.routeDao()
        )

    private val tripRepository =
        TripRepository(
            tripDao = database.tripDao()
        )

    private val _uiState =
        MutableStateFlow(
            DashboardUiState()
        )

    val uiState: StateFlow<DashboardUiState> =
        _uiState.asStateFlow()

    private var locationObservationJob: Job? = null

    init {
        initialiseRoutes()
        observeRoutes()
        loadActiveTrip()
    }

    /**
     * Inserts demonstration routes only when Room is empty.
     *
     * These routes make the application usable on the first launch,
     * including when the device has no internet connection.
     */
    private fun initialiseRoutes() {
        viewModelScope.launch {
            if (routeRepository.getRouteCount() == 0) {
                routeRepository.insertRoutes(
                    listOf(
                        RouteEntity(
                            routeId = "SL0102A",
                            routeName =
                                "Bandar Sri Permaisuri to TBS",
                            origin =
                                "Bandar Sri Permaisuri",
                            destination =
                                "Terminal Bersepadu Selatan",
                            nextStop =
                                "Bandar Tasik Selatan",
                            scheduledTime = "06:30 AM",
                            scheduledTripId = "TR240001",
                            vehicleNumber = "JWR4400",
                            direction = "Outbound"
                        ),
                        RouteEntity(
                            routeId = "SL0103B",
                            routeName =
                                "TBS to Bandar Sri Permaisuri",
                            origin =
                                "Terminal Bersepadu Selatan",
                            destination =
                                "Bandar Sri Permaisuri",
                            nextStop =
                                "Salak Selatan",
                            scheduledTime = "08:15 AM",
                            scheduledTripId = "TR240002",
                            vehicleNumber = "JWR4400",
                            direction = "Inbound"
                        ),
                        RouteEntity(
                            routeId = "KL0201",
                            routeName =
                                "Bandar Tasik Selatan to KL Sentral",
                            origin =
                                "Bandar Tasik Selatan",
                            destination =
                                "KL Sentral",
                            nextStop =
                                "Bukit Bintang",
                            scheduledTime = "11:00 AM",
                            scheduledTripId = "TR240003",
                            vehicleNumber = "JWR4400",
                            direction = "Outbound"
                        ),
                        RouteEntity(
                            routeId = "KL0305",
                            routeName =
                                "KL Sentral to Bukit Bintang",
                            origin =
                                "KL Sentral",
                            destination =
                                "Bukit Bintang",
                            nextStop =
                                "Imbi",
                            scheduledTime = "02:30 PM",
                            scheduledTripId = "TR240004",
                            vehicleNumber = "JWR4400",
                            direction = "Outbound"
                        )
                    )
                )
            }
        }
    }

    /**
     * Observes Room so the dashboard updates automatically whenever
     * local route records are inserted or replaced.
     */
    private fun observeRoutes() {
        viewModelScope.launch {
            routeRepository
                .observeRoutes()
                .collect { routes ->
                    _uiState.value =
                        _uiState.value.copy(
                            routes = routes,
                            isLoading = false
                        )
                }
        }
    }

    /**
     * Restores an unfinished trip after an application restart.
     */
    private fun loadActiveTrip() {
        viewModelScope.launch {
            val activeTrip =
                tripRepository.getActiveTrip()

            _uiState.value =
                _uiState.value.copy(
                    activeTrip = activeTrip,
                    selectedRouteId =
                        activeTrip?.routeId
                )

            if (activeTrip != null) {
                observeLocationPoints(
                    activeTrip.tripId
                )
            }
        }
    }

    /**
     * Saves the selected route ID in the current UI state.
     */
    fun selectRoute(
        routeId: String
    ) {
        _uiState.value =
            _uiState.value.copy(
                selectedRouteId = routeId,
                message = null
            )
    }

    /**
     * Downloads the latest route list when internet access is available.
     *
     * The RouteRepository stores downloaded routes in Room. The dashboard
     * updates automatically because it observes the Room route table.
     */
    fun refreshRoutes() {
        if (_uiState.value.isRefreshingRoutes) {
            return
        }

        if (!isNetworkAvailable()) {
            _uiState.value =
                _uiState.value.copy(
                    message =
                        "No internet connection. Existing offline routes are still available."
                )
            return
        }

        viewModelScope.launch {
            _uiState.value =
                _uiState.value.copy(
                    isRefreshingRoutes = true,
                    message =
                        "Downloading the latest routes..."
                )

            try {
                val refreshedRouteCount =
                    routeRepository.refreshRoutes()

                _uiState.value =
                    _uiState.value.copy(
                        isRefreshingRoutes = false,
                        message =
                            "$refreshedRouteCount routes updated and saved for offline use."
                    )
            } catch (exception: Exception) {
                _uiState.value =
                    _uiState.value.copy(
                        isRefreshingRoutes = false,
                        message =
                            "Unable to refresh routes: " +
                                    (
                                            exception.message
                                                ?: "Unknown error"
                                            )
                    )
            }
        }
    }

    /**
     * Checks whether Android currently has a validated internet connection.
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getApplication<Application>()
                .getSystemService(
                    Context.CONNECTIVITY_SERVICE
                ) as ConnectivityManager

        val activeNetwork =
            connectivityManager.activeNetwork
                ?: return false

        val capabilities =
            connectivityManager.getNetworkCapabilities(
                activeNetwork
            ) ?: return false

        return capabilities.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_INTERNET
        ) &&
                capabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_VALIDATED
                )
    }

    /**
     * Observes the GPS points saved for an active trip and calculates
     * the current speed, mileage and GPS connection state.
     */
    private fun observeLocationPoints(
        tripId: String
    ) {
        locationObservationJob?.cancel()

        locationObservationJob =
            viewModelScope.launch {
                tripRepository
                    .observeLocationPoints(tripId)
                    .collect { points ->
                        val latestPoint =
                            points.lastOrNull()

                        val speedKmh =
                            (
                                    latestPoint?.speed
                                        ?: 0f
                                    ) * 3.6f

                        var totalDistanceMetres = 0f

                        points.zipWithNext {
                                first,
                                second ->

                            val result =
                                FloatArray(1)

                            Location.distanceBetween(
                                first.latitude,
                                first.longitude,
                                second.latitude,
                                second.longitude,
                                result
                            )

                            /*
                             * Ignore extremely inaccurate jumps to prevent
                             * unrealistic mileage when GPS accuracy is poor.
                             */
                            val distanceMetres =
                                result[0]

                            val acceptableAccuracy =
                                first.accuracy <= 100f &&
                                        second.accuracy <= 100f

                            if (
                                acceptableAccuracy &&
                                distanceMetres >= 0f &&
                                distanceMetres <= 1_000f
                            ) {
                                totalDistanceMetres +=
                                    distanceMetres
                            }
                        }

                        _uiState.value =
                            _uiState.value.copy(
                                locationPointCount =
                                    points.size,
                                currentSpeedKmh =
                                    speedKmh.coerceAtLeast(
                                        0f
                                    ),
                                mileageKm =
                                    totalDistanceMetres
                                        .toDouble() /
                                            1_000.0,
                                gpsConnected =
                                    latestPoint != null
                            )
                    }
            }
    }

    /**
     * Creates a new active trip in Room.
     */
    fun startTrip(
        driverId: String
    ) {
        val selectedRouteId =
            _uiState.value.selectedRouteId

        if (selectedRouteId == null) {
            _uiState.value =
                _uiState.value.copy(
                    message =
                        "Please select a route."
                )
            return
        }

        if (_uiState.value.activeTrip != null) {
            _uiState.value =
                _uiState.value.copy(
                    message =
                        "A trip is already active."
                )
            return
        }

        viewModelScope.launch {
            try {
                val trip =
                    TripEntity(
                        driverId = driverId,
                        routeId = selectedRouteId,
                        startTime =
                            System.currentTimeMillis()
                    )

                tripRepository.startTrip(trip)

                _uiState.value =
                    _uiState.value.copy(
                        activeTrip = trip,
                        locationPointCount = 0,
                        currentSpeedKmh = 0f,
                        mileageKm = 0.0,
                        gpsConnected = false,
                        message =
                            "Trip started successfully."
                    )

                observeLocationPoints(
                    trip.tripId
                )
            } catch (exception: Exception) {
                _uiState.value =
                    _uiState.value.copy(
                        message =
                            "Unable to start trip: " +
                                    (
                                            exception.message
                                                ?: "Unknown error"
                                            )
                    )
            }
        }
    }

    /**
     * Completes the active trip and schedules automatic synchronization.
     *
     * The trip is first saved locally as COMPLETED. WorkManager waits for
     * a network connection before attempting the upload.
     */
    fun endTrip(
        endOdometerKm: Double
    ) {
        val activeTrip =
            _uiState.value.activeTrip
                ?: return

        if (endOdometerKm < 0) {
            _uiState.value =
                _uiState.value.copy(
                    message =
                        "Please enter a valid odometer reading."
                )
            return
        }

        viewModelScope.launch {
            try {
                tripRepository.endTrip(
                    tripId = activeTrip.tripId,
                    endTime =
                        System.currentTimeMillis(),
                    endOdometerKm =
                        endOdometerKm
                )

                locationObservationJob?.cancel()
                locationObservationJob = null

                _uiState.value =
                    _uiState.value.copy(
                        activeTrip = null,
                        selectedRouteId = null,
                        locationPointCount = 0,
                        currentSpeedKmh = 0f,
                        mileageKm = 0.0,
                        gpsConnected = false,
                        message =
                            "Trip completed and saved locally. " +
                                    "Synchronization will run when online."
                    )

                SyncScheduler.scheduleTripSync(
                    context = getApplication()
                )
            } catch (exception: Exception) {
                _uiState.value =
                    _uiState.value.copy(
                        message =
                            "Unable to end trip: " +
                                    (
                                            exception.message
                                                ?: "Unknown error"
                                            )
                    )
            }
        }
    }

    /**
     * Clears any temporary information message displayed by the UI.
     */
    fun clearMessage() {
        _uiState.value =
            _uiState.value.copy(
                message = null
            )
    }

    override fun onCleared() {
        locationObservationJob?.cancel()
        super.onCleared()
    }
}