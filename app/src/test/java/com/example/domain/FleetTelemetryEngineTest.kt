package com.example.domain

import com.example.data.*
import org.junit.Assert.*
import org.junit.Test

class FleetTelemetryEngineTest {

    private val baseContributor = FleetContributor(
        id = "fc_test",
        vehicleType = VehicleType.TAXI,
        areaCovered = "Kolonaki",
        deviceStatus = DeviceStatus.ONLINE,
        coverageScore = 0.8,
        dataQualityScore = 0.9,
        monthlyRewardEstimate = 10.0,
        lastHeartbeat = "Just now",
        lastHeartbeatTimestamp = System.currentTimeMillis(),
        privacyMode = "Strict"
    )

    @Test
    fun `data quality score decays with heartbeat latency`() {
        val now = System.currentTimeMillis()
        val staleContributor = baseContributor.copy(lastHeartbeatTimestamp = now - (20 * 60 * 1000)) // 20 mins ago
        
        val scoreFresh = FleetTelemetryEngine.calculateDataQualityScore(baseContributor, now)
        val scoreStale = FleetTelemetryEngine.calculateDataQualityScore(staleContributor, now)
        
        assertTrue("Fresh score ($scoreFresh) should be higher than stale score ($scoreStale)", scoreFresh > scoreStale)
    }

    @Test
    fun `monthly reward applies area multiplier correctly`() {
        val initialReward = 10.0
        val contribution = 1.0
        val quality = 1.0
        
        val rewardExtreme = FleetTelemetryEngine.calculateMonthlyReward(initialReward, contribution, quality, DemandLevel.EXTREME)
        val rewardLow = FleetTelemetryEngine.calculateMonthlyReward(initialReward, contribution, quality, DemandLevel.LOW)
        
        assertTrue("Extreme area reward ($rewardExtreme) should be significantly higher than low area ($rewardLow)", 
            rewardExtreme > rewardLow + 1.0)
    }

    @Test
    fun `device health flags degraded after 10 minutes`() {
        val now = System.currentTimeMillis()
        val healthy = baseContributor.copy(lastHeartbeatTimestamp = now - (2 * 60 * 1000)) // 2 mins ago
        val degraded = baseContributor.copy(lastHeartbeatTimestamp = now - (15 * 60 * 1000)) // 15 mins ago
        val offline = baseContributor.copy(lastHeartbeatTimestamp = now - (70 * 60 * 1000)) // 70 mins ago
        
        assertEquals(DeviceStatus.ONLINE, FleetTelemetryEngine.checkDeviceHealth(healthy, now))
        assertEquals(DeviceStatus.DEGRADED, FleetTelemetryEngine.checkDeviceHealth(degraded, now))
        assertEquals(DeviceStatus.OFFLINE, FleetTelemetryEngine.checkDeviceHealth(offline, now))
    }
}
