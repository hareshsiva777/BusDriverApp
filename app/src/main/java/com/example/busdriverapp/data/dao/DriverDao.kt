package com.example.busdriverapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.busdriverapp.data.model.DriverEntity

@Dao
interface DriverDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriver(driver: DriverEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrivers(drivers: List<DriverEntity>)

    @Query(
        """
        SELECT * FROM drivers
        WHERE driverId = :driverId
        AND password = :password
        LIMIT 1
        """
    )
    suspend fun login(
        driverId: String,
        password: String
    ): DriverEntity?

    @Query("SELECT * FROM drivers WHERE driverId = :driverId LIMIT 1")
    suspend fun getDriver(driverId: String): DriverEntity?

    @Query("SELECT COUNT(*) FROM drivers")
    suspend fun getDriverCount(): Int
}