package com.example.domain

import com.example.data.MockData
import com.example.data.SubscriptionTier
import com.example.data.UserProfile
import com.example.data.local.UserProfileDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class UserRepository(private val userProfileDao: UserProfileDao) {

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val existing = userProfileDao.getProfile().first()
            if (existing == null) {
                userProfileDao.insertProfile(MockData.defaultUserProfile)
            }
        }
    }

    fun getProfile(): Flow<UserProfile?> = userProfileDao.getProfile()

    fun updateCO2Savings(additionalKg: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            val profile = userProfileDao.getProfile().first() ?: return@launch
            userProfileDao.updateProfile(
                profile.copy(totalCO2SavedKg = profile.totalCO2SavedKg + additionalKg)
            )
        }
    }

    fun incrementParkingsFound() {
        CoroutineScope(Dispatchers.IO).launch {
            val profile = userProfileDao.getProfile().first() ?: return@launch
            userProfileDao.updateProfile(
                profile.copy(totalParkingsFound = profile.totalParkingsFound + 1)
            )
        }
    }

    fun updateSubscription(tier: SubscriptionTier) {
        CoroutineScope(Dispatchers.IO).launch {
            val profile = userProfileDao.getProfile().first() ?: return@launch
            userProfileDao.updateProfile(profile.copy(subscriptionTier = tier))
        }
    }

    fun updateReputation(newScore: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            val profile = userProfileDao.getProfile().first() ?: return@launch
            userProfileDao.updateProfile(profile.copy(reputationScore = newScore))
        }
    }
}
