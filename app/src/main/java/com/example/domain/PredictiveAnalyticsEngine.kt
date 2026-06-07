package com.example.domain

import com.example.data.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

object PredictiveAnalyticsEngine {

    /**
     * Calculates the estimated CO2 savings in kg based on reduced cruising time.
     * Formula: Savings = (Typical Search Time - Actual Search Time) * Avg Idle Emission Rate
     * Typical Search Time is derived from Area Demand Level.
     */
    fun calculateCO2Savings(zone: ParkingZone, actualTimeMinutes: Int): Double {
        val typicalSearchTime = when (zone.demandLevel) {
            DemandLevel.EXTREME -> 20.0
            DemandLevel.HIGH -> 12.0
            DemandLevel.MEDIUM -> 6.0
            DemandLevel.LOW -> 3.0
        }
        
        val timeSavedMinutes = max(0.0, typicalSearchTime - actualTimeMinutes)
        // Avg passenger car emits ~0.4 kg CO2 per mile, idling is ~0.03 kg per minute
        val emissionRatePerMinute = 0.032 
        
        return timeSavedMinutes * emissionRatePerMinute
    }

    /**
     * Generates a heuristic probability forecast for a given time offset.
     * Uses a periodic function (sin wave) to simulate urban occupancy cycles
     * combined with the current zone probability.
     */
    fun forecastProbability(currentProb: Double, hourOffset: Int, demandLevel: DemandLevel): Double {
        // Base cycle: high occupancy at 9am and 6pm, low at 3am.
        // We simulate this with a shifted sine wave.
        val cycleImpact = sin(hourOffset.toDouble() * 0.5) * 0.2
        
        val demandVolatility = when (demandLevel) {
            DemandLevel.EXTREME -> 0.4
            DemandLevel.HIGH -> 0.25
            DemandLevel.MEDIUM -> 0.15
            DemandLevel.LOW -> 0.05
        }
        
        val forecasted = currentProb + cycleImpact - (hourOffset * demandVolatility * 0.1)
        return forecasted.coerceIn(0.0, 1.0)
    }

    /**
     * Calculates an Area Efficiency Score (0-100).
     * High efficiency means high occupancy but low cruising pressure.
     */
    fun calculateAreaEfficiency(analytics: MunicipalAreaAnalytics): Int {
        val occupancyWeight = analytics.occupancyRate * 60.0
        val pressurePenalty = analytics.cruisingPressure * 40.0
        val efficiency = occupancyWeight - pressurePenalty + (1.0 - analytics.illegalParkingRisk) * 20.0
        
        return efficiency.toInt().coerceIn(0, 100)
    }

    /**
     * Predicts the peak congestion hour for an area based on current metrics.
     */
    fun predictPeakHour(area: String, currentOccupancy: Double): Int {
        // Simple heuristic: if occupancy is already high, peak is likely soon (within 1-2 hours)
        // If occupancy is low, peak might be further out (4-6 hours)
        return if (currentOccupancy > 0.8) 1 else 5
    }
}
