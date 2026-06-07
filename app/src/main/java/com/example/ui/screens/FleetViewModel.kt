package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FleetContributor
import com.example.domain.FleetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FleetViewModel(private val repository: FleetRepository) : ViewModel() {
    private val _contributors = MutableStateFlow<List<FleetContributor>>(emptyList())
    val contributors: StateFlow<List<FleetContributor>> = _contributors.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getFleetContributors().collect {
                _contributors.value = it
            }
        }
    }

    fun recordHeartbeat(contributorId: String) {
        repository.recordHeartbeat(contributorId)
    }

    fun simulateContribution(contributorId: String) {
        // Simulate a new high-quality signal contribution in a high-demand area
        repository.updateRewards(contributorId, 0.5, com.example.data.DemandLevel.HIGH)
    }
}
