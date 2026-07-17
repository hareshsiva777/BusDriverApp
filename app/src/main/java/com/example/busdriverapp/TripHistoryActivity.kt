package com.example.busdriverapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.busdriverapp.data.model.TripEntity
import com.example.busdriverapp.ui.theme.BusDriverAppTheme
import com.example.busdriverapp.viewmodel.TripHistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class TripHistoryActivity :
    ComponentActivity() {

    private val viewModel:
            TripHistoryViewModel by viewModels()

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            BusDriverAppTheme {

                Surface(
                    modifier =
                        Modifier.fillMaxSize()
                ) {
                    TripHistoryScreen(
                        viewModel = viewModel,
                        onBackClick = {
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripHistoryScreen(
    viewModel: TripHistoryViewModel,
    onBackClick: () -> Unit
) {
    val uiState by
    viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Trip History",
                        fontWeight =
                            FontWeight.Bold
                    )
                },
                navigationIcon = {
                    TextButton(
                        onClick = onBackClick
                    ) {
                        Text("Back")
                    }
                }
            )
        }
    ) { innerPadding ->

        when {

            uiState.isLoading -> {

                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    contentAlignment =
                        Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.message != null -> {

                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(24.dp),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text =
                            uiState.message
                                ?: "Unknown error",
                        style =
                            MaterialTheme
                                .typography
                                .bodyLarge
                    )
                }
            }

            uiState.trips.isEmpty() -> {

                EmptyTripHistory(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                )
            }

            else -> {

                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    contentPadding =
                        PaddingValues(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        ),
                    verticalArrangement =
                        Arrangement.spacedBy(
                            12.dp
                        )
                ) {
                    items(
                        items = uiState.trips,
                        key = { trip ->
                            trip.tripId
                        }
                    ) { trip ->

                        TripHistoryCard(
                            trip = trip
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTripHistory(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                text = "No completed trips",
                style =
                    MaterialTheme
                        .typography
                        .titleLarge,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Text(
                text =
                    "Completed journeys will appear here.",
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium
            )
        }
    }
}

@Composable
private fun TripHistoryCard(
    trip: TripEntity
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 3.dp
            )
    ) {
        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {
                    Text(
                        text =
                            "Route ${trip.routeId}",
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        text =
                            "Driver: ${trip.driverId}",
                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium
                    )
                }

                SyncStatusBadge(
                    isSynced =
                        trip.isSynced
                )
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            HorizontalDivider()

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            HistoryInformationRow(
                label = "Trip ID",
                value =
                    trip.tripId
                        .take(8)
                        .uppercase()
            )
            HistoryInformationRow(
                label = "End odometer",
                value =
                    trip.endOdometerKm?.let {
                        String.format(
                            Locale.getDefault(),
                            "%.1f km",
                            it
                        )
                    } ?: "-"
            )
            HistoryInformationRow(
                label = "Started",
                value =
                    formatDateTime(
                        trip.startTime
                    )
            )

            HistoryInformationRow(
                label = "Ended",
                value =
                    trip.endTime?.let {
                        formatDateTime(it)
                    } ?: "-"
            )

            HistoryInformationRow(
                label = "Duration",
                value =
                    calculateDuration(
                        startTime =
                            trip.startTime,
                        endTime =
                            trip.endTime
                    )
            )

            HistoryInformationRow(
                label = "Status",
                value = trip.status
            )
        }
    }
}

@Composable
private fun HistoryInformationRow(
    label: String,
    value: String
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 4.dp
                ),
        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style =
                MaterialTheme
                    .typography
                    .bodyMedium
        )

        Text(
            text = value,
            style =
                MaterialTheme
                    .typography
                    .bodyMedium,
            fontWeight =
                FontWeight.Medium
        )
    }
}

@Composable
private fun SyncStatusBadge(
    isSynced: Boolean
) {
    Surface(
        shape =
            MaterialTheme.shapes.small,
        tonalElevation = 3.dp
    ) {
        Text(
            text =
                if (isSynced) {
                    "Synced"
                } else {
                    "Pending"
                },
            modifier =
                Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 6.dp
                ),
            style =
                MaterialTheme
                    .typography
                    .labelMedium,
            fontWeight =
                FontWeight.Bold
        )
    }
}

private fun formatDateTime(
    timestamp: Long
): String {
    val formatter =
        SimpleDateFormat(
            "dd MMM yyyy, hh:mm a",
            Locale.getDefault()
        )

    return formatter.format(
        Date(timestamp)
    )
}

private fun calculateDuration(
    startTime: Long,
    endTime: Long?
): String {

    if (endTime == null) {
        return "-"
    }

    val duration =
        (endTime - startTime)
            .coerceAtLeast(0L)

    val hours =
        TimeUnit.MILLISECONDS
            .toHours(duration)

    val minutes =
        TimeUnit.MILLISECONDS
            .toMinutes(duration) % 60

    val seconds =
        TimeUnit.MILLISECONDS
            .toSeconds(duration) % 60

    return when {
        hours > 0 ->
            "${hours}h ${minutes}m"

        minutes > 0 ->
            "${minutes}m ${seconds}s"

        else ->
            "${seconds}s"
    }
}