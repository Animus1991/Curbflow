package com.example.domain

import com.example.data.*
import kotlin.math.max
import kotlin.math.min

object FleetTelemetryEngine {

    /**
     * Calculates the Data Quality Score (DQS) based on signal freshness and status.
     */
    fun calculateDataQualityScore(contributor: FleetContributor, currentTime: Long): Double {
        val latencyMinutes = (currentTime - contributor.lastHeartbeatTimestamp) / (1000 * 60)
        
        val baseScore = when (contributor.deviceStatus) {
            DeviceStatus.ONLINE -> 0.9
            DeviceStatus.DEGRADED -> 0.4
            DeviceStatus.MAINTENANCE -> 0.2
            DeviceStatus.OFFLINE -> 0.0
        }
        
        // Quality decays with latency
        val latencyPenalty = min(0.5, latencyMinutes * 0.05)
        return max(0.0, baseScore - latencyPenalty)
    }

    /**
     * Calculates the estimated monthly reward based on contribution, quality, and area multiplier.
     */
    fun calculateMonthlyReward(
        currentReward: Double,
        newContributionScore: Double,
        qualityScore: Double,
        areaDemandLevel: DemandLevel
    ): Double {
        val areaMultiplier = when (areaDemandLevel) {
            DemandLevel.EXTREME -> 2.5
            DemandLevel.HIGH -> 1.8
            DemandLevel.MEDIUM -> 1.2
            DemandLevel.LOW -> 1.0
        }
        
        val delta = newContributionScore * qualityScore * areaMultiplier
        return currentReward + delta
    }

    /**
     * Determines if a device should be flagged as degraded based on heartbeat consistency.
     */
    fun checkDeviceHealth(contributor: FleetContributor, currentTime: Long): DeviceStatus {
        val secondsSinceLastHeartbeat = (currentTime - contributor.lastHeartbeatTimestamp) / 1000
        
        return when {
            secondsSinceLastHeartbeat > 3600 -> DeviceStatus.OFFLINE // 1 hour
            secondsSinceLastHeartbeat > 600 -> DeviceStatus.DEGRADED // 10 mins
            else -> DeviceStatus.ONLINE
        }
    }
}
