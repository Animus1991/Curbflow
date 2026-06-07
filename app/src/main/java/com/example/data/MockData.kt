package com.example.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object MockData {
    private val initialZones = listOf(
        ParkingZone("z1", "Kolonaki Square", "Kolonaki", "891e00", 37.9776, 23.7441, 0.88, 0.9, 2, 5, 3, 0.8, LegalRisk.LOW, DemandLevel.HIGH, 0.9, "2 min ago", ZoneStatus.HIGH_PROBABILITY, 0.1, 2),
        ParkingZone("z2", "Skoufa St.", "Kolonaki", "891e01", 37.9790, 23.7400, 0.45, 0.7, 5, 8, 5, 0.6, LegalRisk.LOW, DemandLevel.MEDIUM, 0.4, "5 min ago", ZoneStatus.MEDIUM_PROBABILITY, 0.05, 1),
        ParkingZone("z3", "Vasileos Konstantinou", "Pagrati", "891e02", 37.9740, 23.7480, 0.85, 0.8, 1, 2, 8, 0.2, LegalRisk.LOW, DemandLevel.LOW, 0.9, "1 min ago", ZoneStatus.HIGH_PROBABILITY, 0.0, 0),
        ParkingZone("z4", "Syntagma Lower", "Syntagma", "891e03", 37.9750, 23.7340, 0.14, 0.95, 0, 15, 2, 0.95, LegalRisk.RESTRICTED, DemandLevel.EXTREME, 0.05, "Just now", ZoneStatus.LOW_PROBABILITY, 0.8, 5),
        ParkingZone("z5", "Panormou Metro", "Ampelokipoi", "891e04", 37.9940, 23.7660, 0.60, 0.6, 12, 5, 6, 0.5, LegalRisk.LOW, DemandLevel.MEDIUM, 0.5, "12 min ago", ZoneStatus.MEDIUM_PROBABILITY, 0.1, 1),
        ParkingZone("z6", "Mavili Square", "Ampelokipoi", "891e05", 37.9830, 23.7580, 0.25, 0.8, 4, 10, 4, 0.7, LegalRisk.HIGH, DemandLevel.HIGH, 0.2, "4 min ago", ZoneStatus.LOW_PROBABILITY, 0.2, 2),
        ParkingZone("z7", "Koukaki Center", "Koukaki", "891e06", 37.9640, 23.7250, 0.41, 0.7, 6, 7, 5, 0.5, LegalRisk.MEDIUM, DemandLevel.HIGH, 0.5, "6 min ago", ZoneStatus.MEDIUM_PROBABILITY, 0.15, 3),
        ParkingZone("z8", "Zografou Campus", "Zografou", "891e07", 37.9740, 23.7740, 0.75, 0.85, 3, 3, 10, 0.3, LegalRisk.LOW, DemandLevel.MEDIUM, 0.8, "3 min ago", ZoneStatus.HIGH_PROBABILITY, 0.0, 0),
        ParkingZone("z9", "Gkyzi Square", "Gkyzi", "891e08", 37.9900, 23.7450, 0.10, 0.3, 25, 12, 4, 0.8, LegalRisk.HIGH, DemandLevel.HIGH, 0.1, "25 min ago", ZoneStatus.INSUFFICIENT_DATA, 0.3, 4),
        ParkingZone("z10", "Neos Kosmos St", "Neos Kosmos", "891e09", 37.9550, 23.7300, 0.0, 0.1, 45, 10, 5, 0.5, LegalRisk.MEDIUM, DemandLevel.LOW, 0.0, "45 min ago", ZoneStatus.INSUFFICIENT_DATA, 0.0, 0)
    )

    private val _zonesFlow = MutableStateFlow<List<ParkingZone>>(initialZones)
    val zonesFlow: StateFlow<List<ParkingZone>> = _zonesFlow.asStateFlow()

    fun updateZone(newZone: ParkingZone) {
        val list = _zonesFlow.value.toMutableList()
        val index = list.indexOfFirst { it.id == newZone.id }
        if (index != -1) {
            list[index] = newZone
            _zonesFlow.value = list
        }
    }

    val zones: List<ParkingZone> get() = _zonesFlow.value 

    val privateList = listOf(
        PrivateParking("p1", "Polis Park Kolonaki", "Kolonaki", 4.0, 12, 150, 200, 3, 4.2, true, "Active"),
        PrivateParking("p2", "Syntagma Garage", "Syntagma", 6.0, 2, 300, 50, 1, 3.8, true, "Active"),
        PrivateParking("p3", "Pagrati Central Parking", "Pagrati", 3.0, 45, 120, 600, 8, 4.5, true, "Active"),
        PrivateParking("p4", "Ampelokipoi SafePark", "Ampelokipoi", 2.5, 30, 80, 400, 5, 4.0, true, "Active")
    )

    val fleetContributors = listOf(
        FleetContributor("fc1", VehicleType.TAXI, "Kolonaki", DeviceStatus.ONLINE, 0.85, 0.92, 120.0, "1 min ago", 0L, "Strict Metadata"),
        FleetContributor("fc2", VehicleType.DELIVERY_BIKE, "Pagrati", DeviceStatus.ONLINE, 0.65, 0.70, 45.0, "5 min ago", 0L, "Strict Metadata"),
        FleetContributor("fc3", VehicleType.COURIER_VAN, "Syntagma", DeviceStatus.OFFLINE, 0.95, 0.88, 200.0, "2 hours ago", 0L, "Strict Metadata"),
        FleetContributor("fc4", VehicleType.TAXI, "Ampelokipoi", DeviceStatus.ONLINE, 0.75, 0.80, 80.0, "Just now", 0L, "Strict Metadata")
    )

    val sensorEvents = listOf(
        SensorEvent("se1", SensorEventType.EMPTY_SPOT_PROBABILITY, "z1", 0.9, 120, VehicleType.TAXI, true, "2 min ago"),
        SensorEvent("se2", SensorEventType.OCCUPIED, "z2", 0.95, 300, VehicleType.DELIVERY_BIKE, true, "5 min ago"),
        SensorEvent("se3", SensorEventType.ILLEGAL_PARKING_RISK, "z4", 0.85, 60, VehicleType.MUNICIPAL_VEHICLE, true, "1 min ago")
    )

    val municipalData = listOf(
        MunicipalAreaAnalytics("Kolonaki", 0.95, 0.8, 0.7, 15, 120.0, "High", 0.88),
        MunicipalAreaAnalytics("Pagrati", 0.60, 0.4, 0.3, 5, 45.0, "Low", 0.40),
        MunicipalAreaAnalytics("Syntagma", 0.98, 0.9, 0.8, 18, 150.0, "Critical", 0.95),
        MunicipalAreaAnalytics("Ampelokipoi", 0.70, 0.5, 0.4, 7, 60.0, "Medium", 0.60)
    )

    val defaultUserProfile = UserProfile(
        id = "user_demo",
        name = "Demo Driver",
        reputationScore = 4.2,
        totalCO2SavedKg = 3.8,
        totalParkingsFound = 27,
        subscriptionTier = SubscriptionTier.FREE,
        joinedDate = "2025-03-15"
    )

    val sampleBookings = listOf(
        Booking(
            id = "b1",
            userId = "user_demo",
            garageId = "p1",
            garageName = "Polis Park Kolonaki",
            startTime = System.currentTimeMillis() + 3_600_000,
            endTime = System.currentTimeMillis() + 7_200_000,
            price = 8.0,
            status = BookingStatus.CONFIRMED,
            licensePlate = "ABC-1234"
        ),
        Booking(
            id = "b2",
            userId = "user_demo",
            garageId = "p3",
            garageName = "Pagrati Central Parking",
            startTime = System.currentTimeMillis() - 86_400_000,
            endTime = System.currentTimeMillis() - 82_800_000,
            price = 3.0,
            status = BookingStatus.COMPLETED,
            licensePlate = "ABC-1234"
        )
    )
}
