package com.example.busdriverapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.busdriverapp.data.model.RouteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: RouteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutes(routes: List<RouteEntity>)

    @Query("SELECT * FROM routes ORDER BY routeName ASC")
    fun observeRoutes(): Flow<List<RouteEntity>>

    @Query("SELECT * FROM routes ORDER BY routeName ASC")
    suspend fun getRoutes(): List<RouteEntity>

    @Query("SELECT * FROM routes WHERE routeId = :routeId LIMIT 1")
    suspend fun getRoute(routeId: String): RouteEntity?

    @Query("SELECT COUNT(*) FROM routes")
    suspend fun getRouteCount(): Int
}