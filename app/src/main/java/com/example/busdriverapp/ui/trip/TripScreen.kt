package com.example.busdriverapp.ui.trip

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TripScreen(
    routeName: String,
    onEndTrip: () -> Unit
) {

    var seconds by remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(Unit) {

        while (true) {
            kotlinx.coroutines.delay(1000)
            seconds++
        }

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = routeName,
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Trip Duration")

        Text(
            "$seconds seconds",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = onEndTrip
        ) {
            Text("End Trip")
        }

    }

}