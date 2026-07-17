package com.example.busdriverapp.data.api

import com.example.busdriverapp.data.model.RouteEntity
import kotlinx.coroutines.delay

/**
 * Simulates an online route API.
 *
 * In a production application, this object would be replaced by
 * Retrofit or another HTTP client that downloads route data from
 * the real server.
 */
object RouteApiService {

    /**
     * Simulates downloading the latest routes from a server.
     *
     * The delay represents network latency. The returned routes are
     * later saved into Room so they remain available offline.
     */
    suspend fun fetchRoutes(): List<RouteEntity> {
        delay(1_500)

        val updatedAt = System.currentTimeMillis()

        return listOf(
            RouteEntity(
                routeId = "SL0102A",
                routeName = "Bandar Sri Permaisuri to TBS",
                origin = "Bandar Sri Permaisuri",
                destination = "Terminal Bersepadu Selatan",
                nextStop = "Bandar Tasik Selatan",
                scheduledTime = "06:30 AM",
                scheduledTripId = "TR240001",
                vehicleNumber = "JWR4400",
                direction = "Outbound",
                lastUpdated = updatedAt
            ),
            RouteEntity(
                routeId = "SL0103B",
                routeName = "TBS to Bandar Sri Permaisuri",
                origin = "Terminal Bersepadu Selatan",
                destination = "Bandar Sri Permaisuri",
                nextStop = "Salak Selatan",
                scheduledTime = "08:15 AM",
                scheduledTripId = "TR240002",
                vehicleNumber = "JWR4400",
                direction = "Inbound",
                lastUpdated = updatedAt
            ),
            RouteEntity(
                routeId = "KL0201",
                routeName = "Bandar Tasik Selatan to KL Sentral",
                origin = "Bandar Tasik Selatan",
                destination = "KL Sentral",
                nextStop = "Bukit Bintang",
                scheduledTime = "11:00 AM",
                scheduledTripId = "TR240003",
                vehicleNumber = "JWR4400",
                direction = "Outbound",
                lastUpdated = updatedAt
            ),
            RouteEntity(
                routeId = "KL0305",
                routeName = "KL Sentral to Bukit Bintang",
                origin = "KL Sentral",
                destination = "Bukit Bintang",
                nextStop = "Imbi",
                scheduledTime = "02:30 PM",
                scheduledTripId = "TR240004",
                vehicleNumber = "JWR4400",
                direction = "Outbound",
                lastUpdated = updatedAt
            ),
            RouteEntity(
                routeId = "KL0407",
                routeName = "Bukit Bintang to Titiwangsa",
                origin = "Bukit Bintang",
                destination = "Titiwangsa",
                nextStop = "Raja Chulan",
                scheduledTime = "05:00 PM",
                scheduledTripId = "TR240005",
                vehicleNumber = "JWR4400",
                direction = "Outbound",
                lastUpdated = updatedAt
            )
        )
    }
}