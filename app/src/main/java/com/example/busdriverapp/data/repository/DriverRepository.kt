package com.example.busdriverapp.data.repository

import com.example.busdriverapp.data.dao.DriverDao
import com.example.busdriverapp.data.model.DriverEntity

/**
 * Provides one place for all driver-related database operations.
 */
class DriverRepository(
    private val driverDao: DriverDao
) {

    suspend fun insertDriver(
        driver: DriverEntity
    ) {
        driverDao.insertDriver(driver)
    }

    suspend fun insertDrivers(
        drivers: List<DriverEntity>
    ) {
        driverDao.insertDrivers(drivers)
    }

    /**
     * Checks credentials against the locally stored Room record.
     */
    suspend fun login(
        driverId: String,
        password: String
    ): DriverEntity? {
        return driverDao.login(
            driverId = driverId,
            password = password
        )
    }

    suspend fun getDriver(
        driverId: String
    ): DriverEntity? {
        return driverDao.getDriver(driverId)
    }

    suspend fun getDriverCount(): Int {
        return driverDao.getDriverCount()
    }

    /**
     * Creates a local demo account the first time the application runs.
     *
     * This record makes offline login possible during the assessment.
     */
    suspend fun ensureDefaultDriver() {
        if (getDriverCount() == 0) {
            insertDriver(
                DriverEntity(
                    driverId = "DRIVER001",
                    name = "Demo Driver",
                    password = "1234"
                )
            )
        }
    }
}