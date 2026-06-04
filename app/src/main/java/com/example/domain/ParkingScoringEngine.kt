package com.example.domain

import com.example.data.*
import kotlin.math.max

object ParkingScoringEngine {

    fun calculateParkingOpportunityScore(zone: ParkingZone): Double {
        val baseScore = (zone.probability * 0.5) + (zone.supplySignal * 0.5)
        
        val freshnessDecay = max(0.0, 1.0 - (zone.freshnessMinutes / 60.0))
        val adjustedConfidence = zone.confidence * freshnessDecay
        
        val timePenalty = (zone.expectedTimeToParkMinutes + zone.walkingTimeToDestinationMinutes) / 60.0
        val congestionPenalty = zone.congestionImpact * 0.5
        
        val legalRiskPenalty = when (zone.legalRisk) {
            LegalRisk.RESTRICTED -> 1.0
            LegalRisk.HIGH -> 0.8
            LegalRisk.MEDIUM -> 0.4
            LegalRisk.LOW -> 0.1
        }
        
        val demandPenalty = when (zone.demandLevel) {
            DemandLevel.EXTREME -> 0.6
            DemandLevel.HIGH -> 0.4
            DemandLevel.MEDIUM -> 0.2
            DemandLevel.LOW -> 0.0
        }

        val rawScore = (baseScore * adjustedConfidence) - timePenalty - congestionPenalty - legalRiskPenalty - demandPenalty
        return max(0.0, rawScore)
    }

    fun rankParkingZones(zones: List<ParkingZone>): List<ParkingZone> {
        return zones.sortedByDescending { calculateParkingOpportunityScore(it) }
    }

    fun selectBestZone(zones: List<ParkingZone>): ParkingZone? {
        return rankParkingZones(zones).firstOrNull()
    }

    fun decayProbabilityByFreshness(zone: ParkingZone): ParkingZone {
        val decayedProb = max(0.0, zone.probability - (zone.freshnessMinutes * 0.01))
        val newStatus = when {
            zone.freshnessMinutes > 30 -> ZoneStatus.INSUFFICIENT_DATA
            decayedProb > 0.6 -> ZoneStatus.HIGH_PROBABILITY
            decayedProb > 0.3 -> ZoneStatus.MEDIUM_PROBABILITY
            else -> ZoneStatus.LOW_PROBABILITY
        }
        return zone.copy(probability = decayedProb, status = newStatus)
    }

    fun updateZoneAfterUserFeedback(zone: ParkingZone, feedbackType: FeedbackType): ParkingZone {
        val newProb = when (feedbackType) {
            FeedbackType.FOUND_PARKING -> minOf(1.0, zone.probability + 0.3)
            FeedbackType.SPOT_TAKEN -> maxOf(0.0, zone.probability - 0.3)
            FeedbackType.NO_PARKING -> maxOf(0.0, zone.probability - 0.4)
            FeedbackType.RESTRICTED_ZONE -> 0.0
            FeedbackType.CHOSE_PRIVATE_PARKING -> zone.probability
        }
        return zone.copy(
            probability = newProb,
            confidence = 0.95,
            freshnessMinutes = 0,
            lastUpdatedLabel = "Just now",
            status = when {
                newProb > 0.6 -> ZoneStatus.HIGH_PROBABILITY
                newProb > 0.3 -> ZoneStatus.MEDIUM_PROBABILITY
                else -> ZoneStatus.LOW_PROBABILITY
            }
        )
    }

    fun combineStreetProbabilityWithPrivateFallback(zones: List<ParkingZone>, privateParkings: List<PrivateParking>): List<Any> {
        return zones + privateParkings
    }

    fun avoidCrowdingSameZone(zones: List<ParkingZone>, activeUsersByZone: Map<String, Int>): List<ParkingZone> {
        return zones.map { zone ->
            val activeUsers = activeUsersByZone[zone.id] ?: 0
            val crowdingPenalty = activeUsers * 0.05
            zone.copy(probability = max(0.0, zone.probability - crowdingPenalty))
        }
    }
}
