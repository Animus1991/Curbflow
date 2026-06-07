package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.Converters
import com.example.data.Booking
import com.example.data.FleetContributor
import com.example.data.H3CellEntity
import com.example.data.ParkingEventEntity
import com.example.data.ParkingZone
import com.example.data.UserConsent
import com.example.data.UserProfile

@Database(
    entities = [
        ParkingZone::class,
        FleetContributor::class,
        UserProfile::class,
        Booking::class,
        H3CellEntity::class,
        UserConsent::class,
        ParkingEventEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ParkingDatabase : RoomDatabase() {
    abstract fun zoneDao(): ZoneDao
    abstract fun fleetDao(): FleetDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun bookingDao(): BookingDao
    abstract fun h3CellDao(): H3CellDao
    abstract fun consentDao(): ConsentDao
    abstract fun parkingEventDao(): ParkingEventDao

    companion object {
        @Volatile
        private var INSTANCE: ParkingDatabase? = null

        fun getDatabase(context: Context): ParkingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ParkingDatabase::class.java,
                    "parking_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
