package com.example.domain

import com.example.data.FeedbackType
import com.example.data.MockData
import com.example.data.ParkingZone
import com.example.data.local.ZoneDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ParkingRepository(private val zoneDao: ZoneDao) {
    
    init {
        // Initialize the database with mock data if it's empty
        CoroutineScope(Dispatchers.IO).launch {
            val existing = zoneDao.getAllZones().first()
            if (existing.isEmpty()) {
                zoneDao.insertZones(MockData.zones)
            }
        }
    }

    fun getRankedZones(): Flow<List<ParkingZone>> = zoneDao.getAllZones().map { 
        ParkingScoringEngine.rankParkingZones(it) 
    }
    
    fun getZoneById(id: String): Flow<ParkingZone?> = zoneDao.getZoneById(id)

    fun submitParkingOutcome(zoneId: String, feedbackType: FeedbackType) {
        CoroutineScope(Dispatchers.IO).launch {
            val zone = zoneDao.getZoneById(zoneId).first()
            if (zone != null) {
                val updated = ParkingScoringEngine.updateZoneAfterUserFeedback(zone, feedbackType)
                zoneDao.updateZone(updated)
            }
        }
    }
}
