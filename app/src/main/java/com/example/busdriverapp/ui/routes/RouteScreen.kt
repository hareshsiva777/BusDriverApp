package com.example.busdriverapp.ui.routes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RouteScreen(
    onRouteSelected: (String) -> Unit
) {

    val routes = listOf(
        "Route A - City Centre",
        "Route B - Airport",
        "Route C - University",
        "Route D - Bus Terminal",
        "Route E - Shopping Mall"
    )

    var selectedRoute by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            "Select Route",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {

            items(routes) { route ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),

                    onClick = {
                        selectedRoute = route
                    }
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),

                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        RadioButton(
                            selected = selectedRoute == route,
                            onClick = {
                                selectedRoute = route
                            }
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(route)

                    }

                }

            }

        }

        Button(
            onClick = {
                onRouteSelected(selectedRoute)
            },
            enabled = selectedRoute.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Start Trip")

        }

    }

}