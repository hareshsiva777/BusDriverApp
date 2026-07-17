package com.example.busdriverapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.busdriverapp.data.dao.DriverDao
import com.example.busdriverapp.data.dao.RouteDao
import com.example.busdriverapp.data.dao.TripDao
import com.example.busdriverapp.data.model.DriverEntity
import com.example.busdriverapp.data.model.LocationPoint
import com.example.busdriverapp.data.model.RouteEntity
import com.example.busdriverapp.data.model.TripEntity

@Database(
    entities = [
        DriverEntity::class,
        RouteEntity::class,
        TripEntity::class,
        LocationPoint::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun driverDao(): DriverDao

    abstract fun routeDao(): RouteDao

    abstract fun tripDao(): TripDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(
            context: Context
        ): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "bus_driver_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()

                INSTANCE = instance
                instance
            }
        }
    }
}