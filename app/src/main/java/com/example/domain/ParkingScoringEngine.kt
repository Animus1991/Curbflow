package com.example.domain

import com.example.data.*
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

object ParkingScoringEngine {

    /**
     * Calculates a high-fidelity parking opportunity score using a Bayesian-inspired heuristic.
     * Incorporates supply signals, dynamic decay, competitive pressure, and legal/demand risks.
     */
    fun calculateParkingOpportunityScore(zone: ParkingZone): Double {
        // 1. Base Probability Weighted by Supply Signal
        val baseScore = (zone.probability * 0.6) + (zone.supplySignal * 0.4)
        
        // 2. Exponential Freshness Decay based on Demand Intensity
        // High demand areas lose data value much faster than low demand ones.
        val decayConstant = when (zone.demandLevel) {
            DemandLevel.EXTREME -> 0.15 // Very aggressive decay
            DemandLevel.HIGH -> 0.08
            DemandLevel.MEDIUM -> 0.04
            DemandLevel.LOW -> 0.02
        }
        val freshnessDecay = exp(-decayConstant * zone.freshnessMinutes)
        val adjustedConfidence = zone.confidence * freshnessDecay
        
        // 3. Competitive Pressure (Anti-Clustering)
        // If other users are targeting this zone, the score for the current user drops.
        val crowdingPenalty = min(0.5, zone.activeUsers * 0.12)
        
        // 4. Temporal and Operational Penalties
        val timePenalty = (zone.expectedTimeToParkMinutes + zone.walkingTimeToDestinationMinutes) / 45.0
        val congestionPenalty = zone.congestionImpact * 0.4
        
        // 5. Categorical Risk Penalties
        val legalRiskPenalty = when (zone.legalRisk) {
            LegalRisk.RESTRICTED -> 1.0
            LegalRisk.HIGH -> 0.7
            LegalRisk.MEDIUM -> 0.3
            LegalRisk.LOW -> 0.05
        }
        
        val demandPenalty = when (zone.demandLevel) {
            DemandLevel.EXTREME -> 0.5
            DemandLevel.HIGH -> 0.3
            DemandLevel.MEDIUM -> 0.1
            DemandLevel.LOW -> 0.0
        }

        // Final Aggregate Calculation
        val rawScore = (baseScore * adjustedConfidence) - crowdingPenalty - timePenalty - congestionPenalty - legalRiskPenalty - demandPenalty
        
        // Normalized to 0.0 - 1.0 range for UI consistency
        return max(0.0, min(1.0, rawScore))
    }

    fun rankParkingZones(zones: List<ParkingZone>): List<ParkingZone> {
        return zones.sortedByDescending { calculateParkingOpportunityScore(it) }
    }

    fun selectBestZone(zones: List<ParkingZone>): ParkingZone? {
        return rankParkingZones(zones).firstOrNull()
    }

    /**
     * Adjusts the visible probability and status based on data staleness.
     */
    fun decayProbabilityByFreshness(zone: ParkingZone): ParkingZone {
        val decayFactor = when (zone.demandLevel) {
            DemandLevel.EXTREME -> 0.05
            DemandLevel.HIGH -> 0.03
            else -> 0.01
        }
        val decayedProb = max(0.0, zone.probability - (zone.freshnessMinutes * decayFactor))
        
        val newStatus = when {
            zone.freshnessMinutes > 45 -> ZoneStatus.INSUFFICIENT_DATA
            decayedProb > 0.7 -> ZoneStatus.HIGH_PROBABILITY
            decayedProb > 0.4 -> ZoneStatus.MEDIUM_PROBABILITY
            else -> ZoneStatus.LOW_PROBABILITY
        }
        return zone.copy(probability = decayedProb, status = newStatus)
    }

    /**
     * Resets metadata and applies immediate probability shifts based on direct user observation.
     */
    fun updateZoneAfterUserFeedback(zone: ParkingZone, feedbackType: FeedbackType): ParkingZone {
        val newProb = when (feedbackType) {
            FeedbackType.FOUND_PARKING -> min(1.0, zone.probability + 0.4)
            FeedbackType.SPOT_TAKEN -> max(0.0, zone.probability - 0.3)
            FeedbackType.NO_PARKING -> max(0.0, zone.probability - 0.5)
            FeedbackType.RESTRICTED_ZONE -> 0.0
            FeedbackType.CHOSE_PRIVATE_PARKING -> zone.probability
        }
        
        return zone.copy(
            probability = newProb,
            confidence = 1.0, // Direct observation reset
            freshnessMinutes = 0,
            lastUpdatedLabel = "Just now",
            activeUsers = if (feedbackType == FeedbackType.FOUND_PARKING) 0 else zone.activeUsers,
            status = when {
                newProb > 0.7 -> ZoneStatus.HIGH_PROBABILITY
                newProb > 0.4 -> ZoneStatus.MEDIUM_PROBABILITY
                else -> ZoneStatus.LOW_PROBABILITY
            }
        )
    }

    fun combineStreetProbabilityWithPrivateFallback(zones: List<ParkingZone>, privateParkings: List<PrivateParking>): List<Any> {
        return zones + privateParkings
    }

    /**
     * Global adjustment to prevent multiple users from being routed to the same spot.
     */
    fun avoidCrowdingSameZone(zones: List<ParkingZone>, activeUsersByZone: Map<String, Int>): List<ParkingZone> {
        return zones.map { zone ->
            val activeUsers = activeUsersByZone[zone.id] ?: 0
            zone.copy(activeUsers = activeUsers)
        }
    }
}
