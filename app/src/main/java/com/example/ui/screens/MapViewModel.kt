package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ParkingZone
import com.example.domain.ParkingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {
    private val repository = ParkingRepository()
    private val _zones = MutableStateFlow<List<ParkingZone>>(emptyList())
    val zones: StateFlow<List<ParkingZone>> = _zones.asStateFlow()

    private val _speed = MutableStateFlow(0f)
    val speed: StateFlow<Float> = _speed.asStateFlow()
    
    val isDriving: StateFlow<Boolean> = _speed.map { it > 5f }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    private val _favoriteZones = MutableStateFlow<Set<String>>(emptySet())
    val favoriteZones: StateFlow<Set<String>> = _favoriteZones.asStateFlow()

    private val _alertThresholds = MutableStateFlow<Map<String, Float>>(emptyMap())
    val alertThresholds: StateFlow<Map<String, Float>> = _alertThresholds.asStateFlow()

    fun toggleFavorite(zoneId: String) {
        val current = _favoriteZones.value.toMutableSet()
        if (current.contains(zoneId)) {
            current.remove(zoneId)
            // also remove threshold
            val thresholds = _alertThresholds.value.toMutableMap()
            thresholds.remove(zoneId)
            _alertThresholds.value = thresholds
        } else {
            current.add(zoneId)
            // default threshold 0.7
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
        viewModelScope.launch {
            repository.getRankedZones().collect { ranked ->
                _zones.value = ranked
            }
        }
    }

    fun submitOutcome(zoneId: String, feedbackType: com.example.data.FeedbackType) {
        repository.submitParkingOutcome(zoneId, feedbackType)
    }
}
