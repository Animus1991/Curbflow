package com.example.domain

import com.example.data.*
import com.example.data.local.FleetDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FleetRepository(private val fleetDao: FleetDao) {

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val existing = fleetDao.getAllContributors().first()
            if (existing.isEmpty()) {
                fleetDao.insertContributors(MockData.fleetContributors)
            }
        }
    }

    fun getFleetContributors(): Flow<List<FleetContributor>> = fleetDao.getAllContributors()

    fun getRecentSensorEvents(): Flow<List<SensorEvent>> = kotlinx.coroutines.flow.flow {
        emit(MockData.sensorEvents)
    }

    fun recordHeartbeat(contributorId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val contributor = fleetDao.getContributorById(contributorId).first()
            if (contributor != null) {
                val now = System.currentTimeMillis()
                val updated = contributor.copy(
                    lastHeartbeat = "Just now",
                    lastHeartbeatTimestamp = now,
                    deviceStatus = DeviceStatus.ONLINE,
                    dataQualityScore = FleetTelemetryEngine.calculateDataQualityScore(contributor, now)
                )
                fleetDao.updateContributor(updated)
            }
        }
    }

    fun updateRewards(contributorId: String, newContribution: Double, areaDemand: DemandLevel) {
        CoroutineScope(Dispatchers.IO).launch {
            val contributor = fleetDao.getContributorById(contributorId).first()
            if (contributor != null) {
                val newReward = FleetTelemetryEngine.calculateMonthlyReward(
                    contributor.monthlyRewardEstimate,
                    newContribution,
                    contributor.dataQualityScore,
                    areaDemand
                )
                fleetDao.updateContributor(contributor.copy(monthlyRewardEstimate = newReward))
            }
        }
    }
}
