package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ParkingZone
import com.example.data.SensorEvent
import com.example.domain.ParkingRepository
import com.example.domain.RealTimeSimulationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.delay

class MapViewModel(
    private val repository: ParkingRepository,
    private val simulationService: RealTimeSimulationService
) : ViewModel() {
    private val _zones = MutableStateFlow<List<ParkingZone>>(emptyList())
    val zones: StateFlow<List<ParkingZone>> = _zones.asStateFlow()

    private val _speed = MutableStateFlow(0f)
    val speed: StateFlow<Float> = _speed.asStateFlow()
    
    // Hysteresis logic for Driving State to prevent UI flickering in stop-and-go traffic
    private val _isDrivingInternal = _speed.map { it > 15f }.distinctUntilChanged()
    private val _isDrivingDelayed = MutableStateFlow(false)

    val isDriving: StateFlow<Boolean> = _isDrivingDelayed.asStateFlow()

    private val _favoriteZones = MutableStateFlow<Set<String>>(emptySet())
    val favoriteZones: StateFlow<Set<String>> = _favoriteZones.asStateFlow()

    private val _alertThresholds = MutableStateFlow<Map<String, Float>>(emptyMap())
    val alertThresholds: StateFlow<Map<String, Float>> = _alertThresholds.asStateFlow()

    val sensorEvents: SharedFlow<SensorEvent> = simulationService.sensorEvents
    val lastSimulationTick: StateFlow<Long> = simulationService.lastTickTimestamp

    fun toggleFavorite(zoneId: String) {
        val current = _favoriteZones.value.toMutableSet()
        if (current.contains(zoneId)) {
            current.remove(zoneId)
            val thresholds = _alertThresholds.value.toMutableMap()
            thresholds.remove(zoneId)
            _alertThresholds.value = thresholds
        } else {
            current.add(zoneId)
            val thresholds = _alertThresholds.value.toMutableMap()
            thresholds[zoneId] = 0.7f
            _alertThresholds.value = thresholds
        }
        _favoriteZones.value = current
    }

    fun setAlertThreshold(zoneId: String, threshold: Float) {
        val thresholds = _alertThresholds.value.toMutableMap()
        thresholds[zoneId] = threshold
        _alertThresholds.value = thresholds
    }

    fun setSpeed(newSpeed: Float) {
        _speed.value = newSpeed
    }

    init {
        // Collect ranked zones from repository
        viewModelScope.launch {
            repository.getRankedZones().collect { ranked ->
                _zones.value = ranked
            }
        }

        // Start real-time simulation
        simulationService.startSimulation(viewModelScope)

        // Apply hysteresis: Enter driving mode immediately, but leave it with a delay
        viewModelScope.launch {
            _isDrivingInternal.collect { driving ->
                if (driving) {
                    _isDrivingDelayed.value = true
                } else {
                    // Delay leaving driving mode by 2.5 seconds
                    delay(2500)
                    _isDrivingDelayed.value = false
                }
            }
        }
    }

    fun submitOutcome(zoneId: String, feedbackType: com.example.data.FeedbackType) {
        repository.submitParkingOutcome(zoneId, feedbackType)
    }
}
