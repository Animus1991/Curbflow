package com.example.data

enum class LegalRisk { LOW, MEDIUM, HIGH, RESTRICTED }
enum class DemandLevel { LOW, MEDIUM, HIGH, EXTREME }
enum class ZoneStatus { HIGH_PROBABILITY, MEDIUM_PROBABILITY, LOW_PROBABILITY, INSUFFICIENT_DATA }
enum class VehicleType { TAXI, DELIVERY_BIKE, COURIER_VAN, MUNICIPAL_VEHICLE }
enum class DeviceStatus { ONLINE, OFFLINE, DEGRADED, MAINTENANCE }
enum class SensorEventType { EMPTY_SPOT_PROBABILITY, OCCUPIED, BLOCKAGE, LOADING_ZONE, ILLEGAL_PARKING_RISK, ROADWORK, STALE_SIGNAL }

data class ParkingZone(
    val id: String,
    val name: String,
    val area: String,
    val mockH3Index: String,
    val latitude: Double,
    val longitude: Double,
    val probability: Double,
    val confidence: Double,
    val freshnessMinutes: Int,
    val expectedTimeToParkMinutes: Int,
    val walkingTimeToDestinationMinutes: Int,
    val congestionImpact: Double,
    val legalRisk: LegalRisk,
    val demandLevel: DemandLevel,
    val supplySignal: Double,
    val lastUpdatedLabel: String,
    val status: ZoneStatus
)

data class PrivateParking(
    val id: String,
    val name: String,
    val area: String,
    val pricePerHour: Double,
    val availableSlots: Int,
    val totalSlots: Int,
    val distanceMeters: Int,
    val walkingMinutes: Int,
    val rating: Double,
    val reservationAvailable: Boolean,
    val operatorStatus: String
)

data class FleetContributor(
    val id: String,
    val vehicleType: VehicleType,
    val areaCovered: String,
    val deviceStatus: DeviceStatus,
    val coverageScore: Double,
    val dataQualityScore: Double,
    val monthlyRewardEstimate: Double,
    val lastHeartbeat: String,
    val privacyMode: String
)

data class SensorEvent(
    val id: String,
    val eventType: SensorEventType,
    val zoneId: String,
    val confidence: Double,
    val freshnessSeconds: Int,
    val sourceType: VehicleType,
    val privacyFiltered: Boolean,
    val timestampLabel: String
)

data class MunicipalAreaAnalytics(
    val area: String,
    val occupancyRate: Double,
    val cruisingPressure: Double,
    val illegalParkingRisk: Double,
    val avgTimeToParkMinutes: Int,
    val estimatedCO2ImpactKg: Double,
    val enforcementPriority: String,
    val privateParkingUtilization: Double
)

enum class FeedbackType {
    FOUND_PARKING, SPOT_TAKEN, NO_PARKING, RESTRICTED_ZONE, CHOSE_PRIVATE_PARKING
}
