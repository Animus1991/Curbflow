package com.example.domain

import kotlinx.coroutines.delay
import com.example.data.ParkingZone
import com.example.data.LegalRisk
import com.example.data.DemandLevel
import com.example.data.ZoneStatus

object RealTimeParkingService {
    suspend fun queryLocation(lat: Double, lng: Double): List<ParkingZone> {
        delay(1000)
        return List(3) { i ->
            val latOffset = (Math.random() - 0.5) * 0.015
            val lngOffset = (Math.random() - 0.5) * 0.015
            val prob = 0.3 + Math.random() * 0.7
            ParkingZone(
                id = "dynamic_zone_${System.currentTimeMillis()}_$i",
                name = "Searched Zone ${i + 1}",
                area = "Custom Coordinates",
                mockH3Index = "891e0_${System.currentTimeMillis()}_$i",
                latitude = lat + latOffset,
                longitude = lng + lngOffset,
                probability = prob,
                supplySignal = 0.8,
                expectedTimeToParkMinutes = (1..15).random(),
                walkingTimeToDestinationMinutes = (1..10).random(),
                freshnessMinutes = (0..10).random(),
                congestionImpact = (0.1 + Math.random() * 0.8),
                legalRisk = LegalRisk.entries.random(),
                demandLevel = DemandLevel.entries.random(),
                confidence = 0.8,
                lastUpdatedLabel = "Just now",
                status = if (prob > 0.6) ZoneStatus.HIGH_PROBABILITY else ZoneStatus.MEDIUM_PROBABILITY
            )
        }
    }
}
