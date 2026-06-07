package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

enum class LegalRisk { LOW, MEDIUM, HIGH, RESTRICTED }
enum class DemandLevel { LOW, MEDIUM, HIGH, EXTREME }
enum class ZoneStatus { HIGH_PROBABILITY, MEDIUM_PROBABILITY, LOW_PROBABILITY, INSUFFICIENT_DATA }
enum class VehicleType { TAXI, DELIVERY_BIKE, COURIER_VAN, MUNICIPAL_VEHICLE }
enum class DeviceStatus { ONLINE, OFFLINE, DEGRADED, MAINTENANCE }
enum class SensorEventType { EMPTY_SPOT_PROBABILITY, OCCUPIED, BLOCKAGE, LOADING_ZONE, ILLEGAL_PARKING_RISK, ROADWORK, STALE_SIGNAL }

@Entity(tableName = "parking_zones")
data class ParkingZone(
    @PrimaryKey val id: String,
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
    val status: ZoneStatus,
    val competitionScore: Double = 0.0,
    val activeUsers: Int = 0
)

class Converters {
    @TypeConverter fun fromLegalRisk(value: LegalRisk) = value.name
    @TypeConverter fun toLegalRisk(value: String) = LegalRisk.valueOf(value)

    @TypeConverter fun fromDemandLevel(value: DemandLevel) = value.name
    @TypeConverter fun toDemandLevel(value: String) = DemandLevel.valueOf(value)

    @TypeConverter fun fromZoneStatus(value: ZoneStatus) = value.name
    @TypeConverter fun toZoneStatus(value: String) = ZoneStatus.valueOf(value)

    @TypeConverter fun fromVehicleType(value: VehicleType) = value.name
    @TypeConverter fun toVehicleType(value: String) = VehicleType.valueOf(value)

    @TypeConverter fun fromDeviceStatus(value: DeviceStatus) = value.name
    @TypeConverter fun toDeviceStatus(value: String) = DeviceStatus.valueOf(value)

    @TypeConverter fun fromBookingStatus(value: BookingStatus) = value.name
    @TypeConverter fun toBookingStatus(value: String) = BookingStatus.valueOf(value)

    @TypeConverter fun fromSubscriptionTier(value: SubscriptionTier) = value.name
    @TypeConverter fun toSubscriptionTier(value: String) = SubscriptionTier.valueOf(value)
}

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

@Entity(tableName = "fleet_contributors")
data class FleetContributor(
    @PrimaryKey val id: String,
    val vehicleType: VehicleType,
    val areaCovered: String,
    val deviceStatus: DeviceStatus,
    val coverageScore: Double,
    val dataQualityScore: Double,
    val monthlyRewardEstimate: Double,
    val lastHeartbeat: String,
    val lastHeartbeatTimestamp: Long = 0L,
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

enum class BookingStatus { PENDING, CONFIRMED, COMPLETED, CANCELLED }
enum class SubscriptionTier { FREE, COMMUTER, PROFESSIONAL }
enum class WeatherCondition { CLEAR, RAIN, HEAVY_RAIN, SNOW }
enum class ConsentScope { LOCATION, PARKING_HISTORY, ANALYTICS, FLEET_DATA }

@Entity(tableName = "h3_cells")
data class H3CellEntity(
    @PrimaryKey val h3Index: String,
    val latitude: Double,
    val longitude: Double,
    val probability: Float,
    val freshness: Long,
    val eventType: String = "unknown",
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (20 * 60 * 1000)
)

@Entity(tableName = "user_consents")
data class UserConsent(
    @PrimaryKey val id: String,
    val userId: String,
    val scope: String,
    val version: Int = 1,
    val grantedAt: Long = System.currentTimeMillis(),
    val isValid: Boolean = true
)

@Entity(tableName = "parking_events")
data class ParkingEventEntity(
    @PrimaryKey val eventId: String,
    val h3Index: String,
    val eventType: String,
    val confidence: Float,
    val expiresAt: Long = System.currentTimeMillis() + (20 * 60 * 1000),
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: String,
    val name: String,
    val reputationScore: Double,
    val totalCO2SavedKg: Double,
    val totalParkingsFound: Int,
    val subscriptionTier: SubscriptionTier,
    val joinedDate: String
)

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey val id: String,
    val userId: String,
    val garageId: String,
    val garageName: String,
    val startTime: Long,
    val endTime: Long,
    val price: Double,
    val status: BookingStatus,
    val licensePlate: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
