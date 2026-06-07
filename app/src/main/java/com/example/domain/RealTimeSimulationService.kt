package com.example.domain

import com.example.data.ParkingZone
import com.example.data.SensorEvent
import com.example.data.SensorEventType
import com.example.data.VehicleType
import com.example.data.ZoneStatus
import com.example.data.local.ZoneDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class RealTimeSimulationService(private val zoneDao: ZoneDao) {

    private val _sensorEvents = MutableSharedFlow<SensorEvent>(replay = 10, extraBufferCapacity = 50)
    val sensorEvents: SharedFlow<SensorEvent> = _sensorEvents.asSharedFlow()

    private val _lastTickTimestamp = MutableStateFlow(0L)
    val lastTickTimestamp: StateFlow<Long> = _lastTickTimestamp.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    fun startSimulation(scope: CoroutineScope) {
        if (_isRunning.value) return
        _isRunning.value = true

        scope.launch {
            while (_isRunning.value) {
                delay(30_000) // 30-second tick
                performSimulationTick()
            }
        }
    }

    fun stopSimulation() {
        _isRunning.value = false
    }

    private suspend fun performSimulationTick() {
        val zones = zoneDao.getAllZones().first()
        if (zones.isEmpty()) return

        val now = System.currentTimeMillis()
        _lastTickTimestamp.value = now

        zones.forEach { zone ->
            val probabilityShift = Random.nextDouble(-0.12, 0.15)
            val newProbability = max(0.0, min(1.0, zone.probability + probabilityShift))

            val newFreshness = max(0, zone.freshnessMinutes + 1)
            val newConfidence = max(0.1, zone.confidence - Random.nextDouble(0.0, 0.03))

            val newStatus = when {
                newProbability > 0.7 -> ZoneStatus.HIGH_PROBABILITY
                newProbability > 0.4 -> ZoneStatus.MEDIUM_PROBABILITY
                newProbability > 0.15 -> ZoneStatus.LOW_PROBABILITY
                else -> ZoneStatus.INSUFFICIENT_DATA
            }

            val updatedZone = zone.copy(
                probability = newProbability,
                confidence = newConfidence,
                freshnessMinutes = newFreshness,
                lastUpdatedLabel = formatFreshness(newFreshness),
                status = newStatus
            )
            zoneDao.updateZone(updatedZone)

            // Generate a sensor event for ~40% of zones per tick
            if (Random.nextFloat() < 0.4f) {
                emitSensorEvent(zone)
            }
        }
    }

    private suspend fun emitSensorEvent(zone: ParkingZone) {
        val eventTypes = listOf(
            SensorEventType.EMPTY_SPOT_PROBABILITY,
            SensorEventType.OCCUPIED,
            SensorEventType.LOADING_ZONE,
            SensorEventType.STALE_SIGNAL
        )
        val sourceTypes = listOf(
            VehicleType.TAXI,
            VehicleType.DELIVERY_BIKE,
            VehicleType.COURIER_VAN
        )

        val event = SensorEvent(
            id = UUID.randomUUID().toString().take(8),
            eventType = eventTypes.random(),
            zoneId = zone.id,
            confidence = Random.nextDouble(0.5, 0.99),
            freshnessSeconds = Random.nextInt(10, 300),
            sourceType = sourceTypes.random(),
            privacyFiltered = true,
            timestampLabel = "Just now"
        )
        _sensorEvents.emit(event)
    }

    private fun formatFreshness(minutes: Int): String = when {
        minutes < 1 -> "Just now"
        minutes == 1 -> "1 min ago"
        minutes < 60 -> "$minutes min ago"
        else -> "${minutes / 60}h ago"
    }
}
