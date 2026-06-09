package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

        /**
         * Migration v3 → v4: adds privacy-by-design tables
         * (h3_cells, user_consents, parking_events).
         * Non-destructive — all existing user data is preserved.
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `h3_cells` (
                        `h3Index` TEXT NOT NULL PRIMARY KEY,
                        `latitude` REAL NOT NULL,
                        `longitude` REAL NOT NULL,
                        `probability` REAL NOT NULL,
                        `freshness` INTEGER NOT NULL,
                        `eventType` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `expiresAt` INTEGER NOT NULL
                    )"""
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `user_consents` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `userId` TEXT NOT NULL,
                        `scope` TEXT NOT NULL,
                        `version` INTEGER NOT NULL,
                        `grantedAt` INTEGER NOT NULL,
                        `isValid` INTEGER NOT NULL
                    )"""
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `parking_events` (
                        `eventId` TEXT NOT NULL PRIMARY KEY,
                        `h3Index` TEXT NOT NULL,
                        `eventType` TEXT NOT NULL,
                        `confidence` REAL NOT NULL,
                        `expiresAt` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )"""
                )
            }
        }

        fun getDatabase(context: Context): ParkingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ParkingDatabase::class.java,
                    "parking_database"
                )
                    .addMigrations(MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
