package com.example.busdriverapp.data.repository

import com.example.busdriverapp.data.api.RouteApiService
import com.example.busdriverapp.data.dao.RouteDao
import com.example.busdriverapp.data.model.RouteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Provides one place for the ViewModel to access route data.
 *
 * Room is the local source of truth. Online route data is downloaded
 * through RouteApiService and then stored in Room.
 */
class RouteRepository(
    private val routeDao: RouteDao
) {

    /**
     * Continuously observes all routes saved in Room.
     */
    fun observeRoutes(): Flow<List<RouteEntity>> {
        return routeDao.observeRoutes()
    }

    /**
     * Saves or replaces multiple routes in Room.
     */
    suspend fun insertRoutes(
        routes: List<RouteEntity>
    ) {
        routeDao.insertRoutes(routes)
    }

    /**
     * Returns the number of routes currently stored locally.
     */
    suspend fun getRouteCount(): Int {
        return routeDao.getRouteCount()
    }

    /**
     * Simulates downloading the newest routes from an online server.
     *
     * The downloaded routes are saved into Room. Because the dashboard
     * observes Room, the interface updates automatically afterward.
     *
     * @return Number of routes downloaded and stored.
     */
    suspend fun refreshRoutes(): Int {
        val downloadedRoutes =
            RouteApiService.fetchRoutes()

        routeDao.insertRoutes(downloadedRoutes)

        return downloadedRoutes.size
    }
}