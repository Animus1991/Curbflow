package com.example.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.ZoneDao
import com.example.domain.BookingRepository
import com.example.domain.FleetRepository
import com.example.domain.ParkingRepository
import com.example.domain.RealTimeSimulationService
import com.example.domain.UserRepository
import com.example.ui.screens.BookingViewModel
import com.example.ui.screens.FleetViewModel
import com.example.ui.screens.MapViewModel
import com.example.ui.screens.UserViewModel

import com.example.util.LocationManager

class ViewModelFactory(
    private val context: android.content.Context,
    private val database: com.example.data.local.ParkingDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MapViewModel::class.java) -> {
                val repository = ParkingRepository(database.zoneDao())
                val simulationService = RealTimeSimulationService(database.zoneDao())
                val locationManager = LocationManager(context)
                MapViewModel(repository, simulationService, locationManager) as T
            }
            modelClass.isAssignableFrom(FleetViewModel::class.java) -> {
                val repository = FleetRepository(database.fleetDao())
                FleetViewModel(repository) as T
            }
            modelClass.isAssignableFrom(UserViewModel::class.java) -> {
                val repository = UserRepository(database.userProfileDao())
                UserViewModel(repository) as T
            }
            modelClass.isAssignableFrom(BookingViewModel::class.java) -> {
                val repository = BookingRepository(database.bookingDao(), secretKeyProvider = {
                    com.example.domain.SecurityManager.getUserSecretKey(context)
                        ?: com.example.domain.SecurityManager.generateSecretKey().also {
                            com.example.domain.SecurityManager.storeUserSecretKey(context, it)
                        }
                })
                BookingViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
