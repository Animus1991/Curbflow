package com.example.domain

import com.example.data.Booking
import com.example.data.BookingStatus
import com.example.data.local.BookingDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class BookingRepository(private val bookingDao: BookingDao) {

    fun getAllBookings(): Flow<List<Booking>> = bookingDao.getAllBookings()

    fun getActiveBookings(): Flow<List<Booking>> = bookingDao.getActiveBookings()

    fun getBookingById(id: String): Flow<Booking?> = bookingDao.getBookingById(id)

    fun createBooking(
        userId: String,
        garageId: String,
        garageName: String,
        startTime: Long,
        endTime: Long,
        price: Double,
        licensePlate: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val booking = Booking(
                id = UUID.randomUUID().toString(),
                userId = userId,
                garageId = garageId,
                garageName = garageName,
                startTime = startTime,
                endTime = endTime,
                price = price,
                status = BookingStatus.CONFIRMED,
                licensePlate = licensePlate,
                createdAt = System.currentTimeMillis()
            )
            bookingDao.insertBooking(booking)
        }
    }

    fun cancelBooking(bookingId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val booking = bookingDao.getBookingById(bookingId).first() ?: return@launch
            bookingDao.updateBooking(booking.copy(status = BookingStatus.CANCELLED))
        }
    }

    fun completeBooking(bookingId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val booking = bookingDao.getBookingById(bookingId).first() ?: return@launch
            bookingDao.updateBooking(booking.copy(status = BookingStatus.COMPLETED))
        }
    }
}
