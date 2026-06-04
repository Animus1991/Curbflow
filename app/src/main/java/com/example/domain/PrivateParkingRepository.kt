package com.example.domain

import com.example.data.MockData
import com.example.data.PrivateParking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PrivateParkingRepository {
    fun getPrivateParkings(): Flow<List<PrivateParking>> = flow {
        emit(MockData.privateList)
    }

    fun reservePrivateParking(parkingId: String): Boolean {
        return MockData.privateList.any { it.id == parkingId && it.reservationAvailable }
    }
}
