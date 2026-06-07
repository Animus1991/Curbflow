package com.example.data.local

import androidx.room.*
import com.example.data.ParkingEventEntity

@Dao
interface ParkingEventDao {
    @Query("SELECT * FROM parking_events WHERE expiresAt > :now ORDER BY createdAt DESC")
    suspend fun getFreshEvents(now: Long = System.currentTimeMillis()): List<ParkingEventEntity>

    @Query("SELECT * FROM parking_events WHERE h3Index = :h3Index AND expiresAt > :now")
    suspend fun getEventsForCell(h3Index: String, now: Long = System.currentTimeMillis()): List<ParkingEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: ParkingEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<ParkingEventEntity>)

    @Query("DELETE FROM parking_events WHERE expiresAt < :cutoffTime")
    suspend fun deleteExpired(cutoffTime: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM parking_events WHERE expiresAt > :now")
    suspend fun countFreshEvents(now: Long = System.currentTimeMillis()): Int
}
