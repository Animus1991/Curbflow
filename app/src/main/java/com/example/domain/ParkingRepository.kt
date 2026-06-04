package com.example.domain

import com.example.data.FeedbackType
import com.example.data.MockData
import com.example.data.ParkingZone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ParkingRepository {
    fun getRankedZones(): Flow<List<ParkingZone>> = MockData.zonesFlow.map { 
        ParkingScoringEngine.rankParkingZones(it) 
    }
    
    fun getZoneById(id: String): Flow<ParkingZone?> = MockData.zonesFlow.map { 
        it.find { z -> z.id == id } 
    }

    fun submitParkingOutcome(zoneId: String, feedbackType: FeedbackType) {
        val zone = MockData.zonesFlow.value.find { it.id == zoneId }
        if (zone != null) {
            val updated = ParkingScoringEngine.updateZoneAfterUserFeedback(zone, feedbackType)
            MockData.updateZone(updated)
        }
    }
}
