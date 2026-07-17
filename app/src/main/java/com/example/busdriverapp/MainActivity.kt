package com.example.busdriverapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.busdriverapp.data.model.DriverEntity
import com.example.busdriverapp.data.model.RouteEntity
import com.example.busdriverapp.data.model.TripEntity
import com.example.busdriverapp.location.LocationTrackingService
import com.example.busdriverapp.ui.theme.BusDriverAppTheme
import com.example.busdriverapp.viewmodel.DashboardViewModel
import com.example.busdriverapp.viewmodel.LoginViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Main entry point of the Android application.
 *
 * This activity applies the app theme and displays the main
 * BusDriverApp composable.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            BusDriverAppTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    BusDriverApp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

/**
 * Controls the main application navigation flow.
 *
 * Current flow:
 *
 * Login
 * → Dashboard
 * → Route selection
 * → Start confirmation dialog
 * → Journey confirmation
 * → Active trip
 */
@Composable
fun BusDriverApp(
    modifier: Modifier = Modifier
) {
    var isLoggedIn by rememberSaveable {
        mutableStateOf(false)
    }

    var showStartDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showConfirmationScreen by rememberSaveable {
        mutableStateOf(false)
    }

    var isStartingTrip by rememberSaveable {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    /*
     * LoginViewModel validates the driver against the local Room database.
     * The default demo driver is inserted into Room automatically, so login
     * continues to work even when the device has no internet connection.
     */
    val loginViewModel: LoginViewModel = viewModel()
    val loginUiState by loginViewModel.uiState.collectAsState()

    // Move to the dashboard after Room returns a valid driver record.
    LaunchedEffect(loginUiState.driver?.driverId) {
        if (loginUiState.driver != null) {
            isLoggedIn = true
        }
    }

    // Show the Room-backed login screen until authentication succeeds.
    if (!isLoggedIn || loginUiState.driver == null) {
        LoginScreen(
            modifier = modifier,
            isLoading = loginUiState.isLoading,
            errorMessage = loginUiState.errorMessage,
            onLogin = { driverId, password ->
                loginViewModel.login(
                    driverId = driverId,
                    password = password
                )
            },
            onInputChanged = {
                loginViewModel.clearError()
            }
        )
        return
    }

    // The authenticated driver is now available for every trip screen.
    val currentDriver: DriverEntity = loginUiState.driver
        ?: return

    val dashboardViewModel: DashboardViewModel = viewModel()
    val uiState by dashboardViewModel.uiState.collectAsState()

    // Find the full route object using the selected route ID.
    val selectedRoute = uiState.routes.firstOrNull {
        it.routeId == uiState.selectedRouteId
    }

    /**
     * Requests location and notification permissions.
     *
     * When location permission is approved, the trip is created
     * in the local Room database.
     */
    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val fineGranted =
                permissions[
                    Manifest.permission.ACCESS_FINE_LOCATION
                ] == true ||
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

            val coarseGranted =
                permissions[
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ] == true ||
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

            if (fineGranted || coarseGranted) {
                dashboardViewModel.startTrip(
                    driverId = currentDriver.driverId
                )
            } else {
                isStartingTrip = false
            }
        }

    /**
     * Starts a trip immediately if permission already exists.
     *
     * Otherwise, it asks the driver for location permission first.
     */
    fun startTripWithPermission() {
        if (isStartingTrip) {
            return
        }

        isStartingTrip = true

        val fineGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            dashboardViewModel.startTrip(
                driverId = currentDriver.driverId
            )
        } else {
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

            // Android 13 and newer require notification permission.
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.TIRAMISU
            ) {
                permissions.add(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }

            permissionLauncher.launch(
                permissions.toTypedArray()
            )
        }
    }

    /**
     * Starts the foreground GPS tracking service whenever
     * an active trip appears in the ViewModel.
     */
    LaunchedEffect(uiState.activeTrip?.tripId) {
        val activeTrip = uiState.activeTrip

        if (activeTrip != null) {
            isStartingTrip = false
            showConfirmationScreen = false
            showStartDialog = false

            val fineGranted =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

            val coarseGranted =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

            if (fineGranted || coarseGranted) {
                val startIntent = Intent(
                    context,
                    LocationTrackingService::class.java
                ).apply {
                    action =
                        LocationTrackingService.ACTION_START

                    putExtra(
                        LocationTrackingService.EXTRA_TRIP_ID,
                        activeTrip.tripId
                    )
                }

                ContextCompat.startForegroundService(
                    context,
                    startIntent
                )
            }
        }
    }

    // Decide which screen should currently be displayed.
    when {
        uiState.activeTrip != null -> {
            ActiveTripScreen(
                modifier = modifier,
                trip = uiState.activeTrip!!,
                route = uiState.routes.firstOrNull {
                    it.routeId == uiState.activeTrip!!.routeId
                },
                locationPointCount = uiState.locationPointCount,
                currentSpeedKmh = uiState.currentSpeedKmh,
                mileageKm = uiState.mileageKm,
                gpsConnected = uiState.gpsConnected,
                message = uiState.message,
                onEndTrip = { endOdometerKm ->

                    val stopIntent = Intent(
                        context,
                        LocationTrackingService::class.java
                    ).apply {
                        action =
                            LocationTrackingService.ACTION_STOP
                    }

                    context.startService(stopIntent)

                    dashboardViewModel.endTrip(
                        endOdometerKm =
                            endOdometerKm
                    )

                    showConfirmationScreen = false
                    showStartDialog = false
                    isStartingTrip = false
                }
            )
        }

        showConfirmationScreen && selectedRoute != null -> {
            JourneyConfirmationScreen(
                modifier = modifier,
                driverName = currentDriver.name,
                driverId = currentDriver.driverId,
                route = selectedRoute,
                isStartingTrip = isStartingTrip,
                onBack = {
                    showConfirmationScreen = false
                    isStartingTrip = false
                },
                onStartJourney = {
                    startTripWithPermission()
                }
            )
        }

        else -> {
            DashboardScreen(
                modifier = modifier,
                routes = uiState.routes,
                selectedRouteId = uiState.selectedRouteId,
                isLoading = uiState.isLoading,
                isRefreshingRoutes = uiState.isRefreshingRoutes,
                message = uiState.message,

                onRouteSelected = dashboardViewModel::selectRoute,
                onContinue = {
                    if (selectedRoute != null) {
                        showStartDialog = true
                    }
                },
                onRefreshRoutes = {
                    dashboardViewModel.refreshRoutes()
                },
                onTripHistory = {
                    context.startActivity(
                        Intent(
                            context,
                            TripHistoryActivity::class.java
                        )
                    )
                },
                onLogout = {
                    loginViewModel.logout()
                    isLoggedIn = false
                    showStartDialog = false
                    showConfirmationScreen = false
                    isStartingTrip = false
                }
            )
        }
    }

    // Display the first trip confirmation dialog.
    if (showStartDialog && selectedRoute != null) {
        StartTripDialog(
            route = selectedRoute,
            driverName = currentDriver.name,
            driverId = currentDriver.driverId,
            onDismiss = {
                showStartDialog = false
            },
            onConfirm = {
                showStartDialog = false
                showConfirmationScreen = true
            }
        )
    }
}

/**
 * Displays the offline-capable driver login screen.
 *
 * Credentials are checked against the local Room database.
 * This allows the driver to log in without an internet connection.
 */
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    errorMessage: String?,
    onLogin: (String, String) -> Unit,
    onInputChanged: () -> Unit
) {
    var driverId by rememberSaveable {
        mutableStateOf("")
    }

    var password by rememberSaveable {
        mutableStateOf("")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bus Driver App",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Offline Driver Login",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Credentials are verified from local device storage.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = driverId,
            onValueChange = { value ->
                driverId = value
                onInputChanged()
            },
            label = {
                Text("Driver ID")
            },
            enabled = !isLoading,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { value ->
                password = value
                onInputChanged()
            },
            label = {
                Text("Password")
            },
            enabled = !isLoading,
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (!errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onLogin(
                    driverId.trim(),
                    password
                )
            },
            enabled =
                !isLoading &&
                        driverId.isNotBlank() &&
                        password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Login")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Demo account: DRIVER001 / 1234",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Displays the driver's available scheduled routes.
 *
 * The driver selects a route from the dropdown and presses Continue.
 */
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    routes: List<RouteEntity>,
    selectedRouteId: String?,
    isLoading: Boolean,
    isRefreshingRoutes: Boolean,
    message: String?,
    onRouteSelected: (String) -> Unit,
    onContinue: () -> Unit,
    onRefreshRoutes: () -> Unit,
    onTripHistory: () -> Unit,
    onLogout: () -> Unit
) {
    var dropdownExpanded by remember {
        mutableStateOf(false)
    }

    val selectedRoute = routes.firstOrNull {
        it.routeId == selectedRouteId
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(
                    rememberScrollState()
                )
        ) {
            Text(
                text = "Driver Dashboard",
                style =
                    MaterialTheme.typography
                        .headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )
            Text(
                text = "Welcome, Demo Driver"
            )

            Text(
                text = "Driver ID: DRIVER001"
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier =
                        Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "Driver Status",
                        style =
                            MaterialTheme.typography
                                .titleMedium,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    TripMetricRow(
                        label = "Current Status",
                        value = "Available"
                    )

                    TripMetricRow(
                        label = "Local Storage",
                        value = "Active"
                    )

                    TripMetricRow(
                        label = "Background Sync",
                        value = "Enabled"
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = "Select Scheduled Route",
                style =
                    MaterialTheme.typography
                        .titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier =
                        Modifier.align(
                            Alignment.CenterHorizontally
                        )
                )
            } else {
                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value =
                            selectedRoute?.routeName
                                ?: "Select a route",
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text("Route")
                        },
                        modifier =
                            Modifier.fillMaxWidth()
                    )

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable {
                                dropdownExpanded = true
                            }
                    )

                    DropdownMenu(
                        expanded =
                            dropdownExpanded,
                        onDismissRequest = {
                            dropdownExpanded = false
                        },
                        modifier =
                            Modifier.fillMaxWidth(
                                0.88f
                            )
                    ) {
                        routes.forEach { route ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text =
                                                route.routeName,
                                            style =
                                                MaterialTheme
                                                    .typography
                                                    .titleSmall
                                        )

                                        Text(
                                            text =
                                                "${route.scheduledTime} • " +
                                                        route.vehicleNumber,
                                            style =
                                                MaterialTheme
                                                    .typography
                                                    .bodySmall
                                        )
                                    }
                                },
                                onClick = {
                                    onRouteSelected(
                                        route.routeId
                                    )

                                    dropdownExpanded =
                                        false
                                }
                            )
                        }
                    }
                }
            }

            if (selectedRoute != null) {
                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )

                SelectedRouteCard(
                    route = selectedRoute
                )
            }

            if (!message.isNullOrBlank()) {
                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                Text(
                    text = message,
                    textAlign =
                        TextAlign.Center,
                    modifier =
                        Modifier.fillMaxWidth()
                )
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )
        }

        OutlinedButton(
            onClick = onTripHistory,
            modifier =
                Modifier.fillMaxWidth()
        ) {
            Text("View Trip History")
        }
        OutlinedButton(
            onClick = onRefreshRoutes,
            enabled = !isRefreshingRoutes,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isRefreshingRoutes) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )

                Spacer(
                    modifier = Modifier.size(10.dp)
                )

                Text("Refreshing Routes...")
            } else {
                Text("Refresh Routes")
            }
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )
        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Button(
            onClick = onContinue,
            enabled = selectedRoute != null,
            modifier =
                Modifier.fillMaxWidth()
        ) {
            Text("Continue")
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        OutlinedButton(
            onClick = onLogout,
            modifier =
                Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
    }
}

/**
 * Displays the details of the currently selected route.
 */
@Composable
fun SelectedRouteCard(
    route: RouteEntity
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = route.routeName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text =
                    "${route.origin} → ${route.destination}"
            )

            Text(
                text = "Route ID: ${route.routeId}"
            )

            Text(
                text = "Vehicle: ${route.vehicleNumber}"
            )

            Text(
                text = "Direction: ${route.direction}"
            )

            Text(
                text = "Scheduled: ${route.scheduledTime}"
            )

            if (route.nextStop.isNotBlank()) {
                Text(
                    text = "Next stop: ${route.nextStop}"
                )
            }
        }
    }
}

/**
 * Displays a confirmation dialog before moving to the
 * final journey confirmation screen.
 */
@Composable
fun StartTripDialog(
    route: RouteEntity,
    driverName: String,
    driverId: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Start Scheduled Trip?")
        },
        text = {
            Column {
                Text("Driver: $driverName")
                Text("Driver ID: $driverId")

                Spacer(modifier = Modifier.height(10.dp))

                Text("Route: ${route.routeName}")
                Text("Route ID: ${route.routeId}")
                Text("Vehicle: ${route.vehicleNumber}")
                Text("Scheduled: ${route.scheduledTime}")
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Continue")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Displays all journey details before starting the GPS service.
 *
 * The driver must slide the control to start the journey.
 */
@Composable
fun JourneyConfirmationScreen(
    modifier: Modifier = Modifier,
    driverName: String,
    driverId: String,
    route: RouteEntity,
    isStartingTrip: Boolean,
    onBack: () -> Unit,
    onStartJourney: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Confirm Your Journey",
                style =
                    MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text =
                    "Please verify all details before starting.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    ConfirmationRow(
                        label = "Driver",
                        value = driverName
                    )

                    ConfirmationRow(
                        label = "Driver ID",
                        value = driverId
                    )

                    ConfirmationRow(
                        label = "Route",
                        value = route.routeName
                    )

                    ConfirmationRow(
                        label = "Route ID",
                        value = route.routeId
                    )

                    ConfirmationRow(
                        label = "Vehicle",
                        value = route.vehicleNumber
                    )

                    ConfirmationRow(
                        label = "Direction",
                        value = route.direction
                    )

                    ConfirmationRow(
                        label = "Scheduled Time",
                        value = route.scheduledTime
                    )

                    if (route.nextStop.isNotBlank()) {
                        ConfirmationRow(
                            label = "Next Stop",
                            value = route.nextStop
                        )
                    }

                    ConfirmationRow(
                        label = "Schedule Trip ID",
                        value = route.scheduledTripId
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text =
                    "Ensure the selected driver, route and " +
                            "vehicle details are correct.",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        if (isStartingTrip) {
            CircularProgressIndicator(
                modifier = Modifier.align(
                    Alignment.CenterHorizontally
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Starting trip…",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            SlideToStartJourney(
                enabled = true,
                onJourneyStart = onStartJourney
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onBack,
            enabled = !isStartingTrip,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}

/**
 * Displays one label and value in the journey confirmation card.
 */
@Composable
fun ConfirmationRow(
    label: String,
    value: String
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary
    )

    Text(
        text = value,
        style = MaterialTheme.typography.bodyLarge
    )

    Spacer(modifier = Modifier.height(12.dp))
}

/**
 * Custom slide control used to prevent accidental trip starts.
 *
 * The driver must move the circular handle at least 85%
 * across the container.
 */
@Composable
fun SlideToStartJourney(
    enabled: Boolean = true,
    onJourneyStart: () -> Unit
) {
    var sliderPosition by remember {
        mutableFloatStateOf(0f)
    }

    var containerWidth by remember {
        mutableFloatStateOf(0f)
    }

    val handleSize = 56.dp

    val handleSizePx = with(LocalDensity.current) {
        handleSize.toPx()
    }

    val maximumPosition =
        (containerWidth - handleSizePx)
            .coerceAtLeast(0f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(
                if (enabled) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .onSizeChanged {
                containerWidth = it.width.toFloat()
            }
    ) {
        Text(
            text = if (enabled) {
                "Slide to start journey"
            } else {
                "Complete the required details"
            },
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.titleMedium
        )

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        sliderPosition.roundToInt(),
                        0
                    )
                }
                .padding(4.dp)
                .size(handleSize)
                .clip(CircleShape)
                .background(
                    if (enabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
                .pointerInput(
                    enabled,
                    maximumPosition
                ) {
                    if (!enabled) {
                        return@pointerInput
                    }

                    detectHorizontalDragGestures(
                        onHorizontalDrag = {
                                change,
                                dragAmount ->

                            change.consume()

                            sliderPosition =
                                (sliderPosition + dragAmount)
                                    .coerceIn(
                                        0f,
                                        maximumPosition
                                    )
                        },
                        onDragEnd = {
                            val completed =
                                maximumPosition > 0f &&
                                        sliderPosition >=
                                        maximumPosition * 0.85f

                            if (completed) {
                                sliderPosition =
                                    maximumPosition

                                onJourneyStart()
                            } else {
                                sliderPosition = 0f
                            }
                        },
                        onDragCancel = {
                            sliderPosition = 0f
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "➜",
                color =
                    MaterialTheme.colorScheme.onPrimary,
                style =
                    MaterialTheme.typography.headlineSmall
            )
        }
    }
}

/**
 * Displays the currently active trip.
 *
 * The trip details scroll independently, while the End Journey
 * button remains fixed and always visible at the bottom.
 */
@Composable
fun ActiveTripScreen(
    modifier: Modifier = Modifier,
    trip: TripEntity,
    route: RouteEntity?,
    locationPointCount: Int,
    currentSpeedKmh: Float,
    mileageKm: Double,
    gpsConnected: Boolean,
    message: String?,
    onEndTrip: (Double) -> Unit
) {
    var currentTime by remember {
        mutableStateOf(System.currentTimeMillis())
    }

    var showEndTripDialog by rememberSaveable {
        mutableStateOf(false)
    }

    // Update the displayed duration every second.
    LaunchedEffect(trip.tripId) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1_000)
        }
    }

    val durationSeconds =
        ((currentTime - trip.startTime) / 1_000)
            .coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = 20.dp,
                top = 16.dp,
                end = 20.dp,
                bottom = 16.dp
            )
    ) {
        /*
         * Only this section scrolls.
         *
         * The End Journey button below stays visible even on
         * smaller phone screens.
         */
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                text = "Trip in Progress",
                style =
                    MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (gpsConnected) {
                    "GPS connected"
                } else {
                    "Waiting for GPS"
                },
                color = if (gpsConnected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text =
                            route?.routeName
                                ?: trip.routeId,
                        style =
                            MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (route != null) {
                        Text(
                            text =
                                "${route.origin} → " +
                                        route.destination
                        )

                        Text(
                            text =
                                "Vehicle: ${route.vehicleNumber}"
                        )

                        Text(
                            text =
                                "Direction: ${route.direction}"
                        )

                        Text(
                            text =
                                "Scheduled: ${route.scheduledTime}"
                        )

                        Text(
                            text =
                                "Schedule Trip ID: " +
                                        route.scheduledTripId
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Driver: ${trip.driverId}"
                    )

                    Text(
                        text =
                            "Started: ${
                                formatTime(trip.startTime)
                            }"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    TripMetricRow(
                        label = "Duration",
                        value =
                            formatDuration(durationSeconds)
                    )

                    TripMetricRow(
                        label = "Speed",
                        value = String.format(
                            Locale.getDefault(),
                            "%.1f km/h",
                            currentSpeedKmh
                        )
                    )

                    TripMetricRow(
                        label = "Mileage",
                        value = String.format(
                            Locale.getDefault(),
                            "%.2f km",
                            mileageKm
                        )
                    )

                    TripMetricRow(
                        label = "GPS",
                        value = if (gpsConnected) {
                            "Connected"
                        } else {
                            "Waiting"
                        }
                    )

                    TripMetricRow(
                        label = "GPS points",
                        value =
                            locationPointCount.toString()
                    )

                    TripMetricRow(
                        label = "Storage",
                        value = "Saving locally"
                    )

                    TripMetricRow(
                        label = "Sync",
                        value = if (trip.isSynced) {
                            "Synced"
                        } else {
                            "Pending"
                        }
                    )

                    TripMetricRow(
                        label = "Status",
                        value = trip.status
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Show the stop the driver is currently heading toward.
            if (!route?.nextStop.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Next Stop",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = route?.nextStop ?: "-",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text =
                                "Final destination: " +
                                        (route?.destination ?: "-"),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = when {
                    !gpsConnected ->
                        "Waiting for the first GPS location…"

                    locationPointCount > 0 ->
                        "Location data is being saved locally."

                    else ->
                        "Starting location tracking…"
                },
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )

            if (!message.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = message,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Fixed button section.
        Button(
            onClick = {
                showEndTripDialog = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("End Journey")
        }
    }

    // Prevent the driver from accidentally ending the journey.
    if (showEndTripDialog) {
        EndTripConfirmationDialog(
            routeName =
                route?.routeName ?: trip.routeId,
            duration =
                formatDuration(durationSeconds),
            mileageKm = mileageKm,
            onDismiss = {
                showEndTripDialog = false
            },
            onConfirm = { endOdometerKm ->
                showEndTripDialog = false
                onEndTrip(endOdometerKm)
            }
        )
    }
}

/**
 * Shows a confirmation dialog before completing the active trip.
 */
@Composable
fun EndTripConfirmationDialog(
    routeName: String,
    duration: String,
    mileageKm: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var odometerText by rememberSaveable {
        mutableStateOf("")
    }

    var errorMessage by rememberSaveable {
        mutableStateOf("")
    }

    val odometerValue =
        odometerText
            .trim()
            .toDoubleOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("End Journey")
        },
        text = {
            Column {
                Text(
                    text =
                        "Enter the vehicle's current " +
                                "odometer reading before ending."
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = "Route: $routeName"
                )

                Text(
                    text = "Duration: $duration"
                )

                Text(
                    text = String.format(
                        Locale.getDefault(),
                        "GPS mileage: %.2f km",
                        mileageKm
                    )
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                OutlinedTextField(
                    value = odometerText,
                    onValueChange = { value ->

                        odometerText =
                            value.filter {
                                it.isDigit() ||
                                        it == '.'
                            }

                        errorMessage = ""
                    },
                    label = {
                        Text("End odometer (km)")
                    },
                    placeholder = {
                        Text("Example: 125430.5")
                    },
                    singleLine = true,
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Decimal
                        ),
                    isError =
                        errorMessage.isNotBlank(),
                    supportingText = {
                        if (
                            errorMessage.isNotBlank()
                        ) {
                            Text(errorMessage)
                        }
                    },
                    modifier =
                        Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Text(
                    text =
                        "The trip and GPS records will be " +
                                "stored locally until synchronization.",
                    style =
                        MaterialTheme.typography
                            .bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        odometerText.isBlank() -> {
                            errorMessage =
                                "Odometer reading is required."
                        }

                        odometerValue == null -> {
                            errorMessage =
                                "Enter a valid number."
                        }

                        odometerValue < 0 -> {
                            errorMessage =
                                "Odometer cannot be negative."
                        }

                        else -> {
                            onConfirm(
                                odometerValue
                            )
                        }
                    }
                }
            ) {
                Text("End Journey")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Continue Trip")
            }
        }
    )
}

/**
 * Displays one metric inside the active trip card.
 *
 * Example:
 * Speed          42.5 km/h
 */
@Composable
fun TripMetricRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement =
            Arrangement.SpaceBetween,
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Converts total seconds into HH:MM:SS format.
 *
 * Example:
 * 65 seconds → 00:01:05
 */
fun formatDuration(
    totalSeconds: Long
): String {
    val hours = totalSeconds / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60

    return String.format(
        Locale.getDefault(),
        "%02d:%02d:%02d",
        hours,
        minutes,
        seconds
    )
}

/**
 * Converts a timestamp into a readable date and time.
 */
fun formatTime(
    timestamp: Long
): String {
    val formatter = SimpleDateFormat(
        "dd MMM yyyy, hh:mm:ss a",
        Locale.getDefault()
    )

    return formatter.format(
        Date(timestamp)
    )
}