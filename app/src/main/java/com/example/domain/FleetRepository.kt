package com.example.domain

import com.example.data.FleetContributor
import com.example.data.MockData
import com.example.data.SensorEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FleetRepository {
    fun getFleetContributors(): Flow<List<FleetContributor>> = flow {
        emit(MockData.fleetContributors)
    }

    fun getRecentSensorEvents(): Flow<List<SensorEvent>> = flow {
        emit(MockData.sensorEvents)
    }

    fun simulateSensorHeartbeat() {
        // Option to trigger mock local update
    }
}
