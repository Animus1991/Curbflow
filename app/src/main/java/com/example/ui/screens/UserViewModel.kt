package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SubscriptionTier
import com.example.data.UserProfile
import com.example.domain.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) : ViewModel() {
    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getProfile().collect { _profile.value = it }
        }
    }

    fun updateSubscription(tier: SubscriptionTier) {
        repository.updateSubscription(tier)
    }

    fun addCO2Savings(kg: Double) {
        repository.updateCO2Savings(kg)
    }

    fun incrementParkingsFound() {
        repository.incrementParkingsFound()
    }

    fun updateReputation(newScore: Double) {
        repository.updateReputation(newScore)
    }
}
