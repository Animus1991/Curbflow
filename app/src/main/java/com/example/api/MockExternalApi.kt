package com.example.api

import kotlinx.coroutines.delay

data class DynamicSpot(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val probability: Float,
    val timestamp: Long
)

object MockExternalApi {
    suspend fun fetchHighProbabilitySpots(centerLat: Double, centerLon: Double): List<DynamicSpot> {
        delay(800) // Simulate network delay
        
        // Generate mock spots around the center
        return List(5) { i ->
            val latOffset = (Math.random() - 0.5) * 0.015
            val lonOffset = (Math.random() - 0.5) * 0.015
            DynamicSpot(
                id = "spot_API_$i",
                latitude = centerLat + latOffset,
                longitude = centerLon + lonOffset,
                probability = 0.7f + (Math.random() * 0.3).toFloat(), // 0.7 to 1.0
                timestamp = System.currentTimeMillis()
            )
        }
    }
}
