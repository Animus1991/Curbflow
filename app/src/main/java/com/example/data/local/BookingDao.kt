package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.Booking
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY createdAt DESC")
    fun getAllBookings(): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE status IN ('PENDING', 'CONFIRMED') ORDER BY startTime ASC")
    fun getActiveBookings(): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE id = :id")
    fun getBookingById(id: String): Flow<Booking?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking)

    @Update
    suspend fun updateBooking(booking: Booking)
}
