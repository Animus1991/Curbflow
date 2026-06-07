package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Booking
import com.example.domain.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookingViewModel(private val repository: BookingRepository) : ViewModel() {
    private val _activeBookings = MutableStateFlow<List<Booking>>(emptyList())
    val activeBookings: StateFlow<List<Booking>> = _activeBookings.asStateFlow()

    private val _allBookings = MutableStateFlow<List<Booking>>(emptyList())
    val allBookings: StateFlow<List<Booking>> = _allBookings.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getActiveBookings().collect { _activeBookings.value = it }
        }
        viewModelScope.launch {
            repository.getAllBookings().collect { _allBookings.value = it }
        }
    }

    fun createBooking(
        userId: String,
        garageId: String,
        garageName: String,
        startTime: Long,
        endTime: Long,
        price: Double,
        licensePlate: String
    ) {
        repository.createBooking(userId, garageId, garageName, startTime, endTime, price, licensePlate)
    }

    fun cancelBooking(bookingId: String) {
        repository.cancelBooking(bookingId)
    }

    fun completeBooking(bookingId: String) {
        repository.completeBooking(bookingId)
    }
}
