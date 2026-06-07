package com.example.domain

import com.example.data.*
import org.junit.Assert.*
import org.junit.Test

class PredictiveAnalyticsEngineTest {

    @Test
    fun `CO2 savings increases with demand level`() {
        val zoneHigh = ParkingZone(
            id = "z1", name = "High", area = "A1", mockH3Index = "", 
            latitude = 0.0, longitude = 0.0, probability = 0.5, confidence = 1.0, 
            freshnessMinutes = 0, expectedTimeToParkMinutes = 2, 
            walkingTimeToDestinationMinutes = 2, congestionImpact = 0.1, 
            legalRisk = LegalRisk.LOW, demandLevel = DemandLevel.HIGH, 
            supplySignal = 0.5, lastUpdatedLabel = "", status = ZoneStatus.MEDIUM_PROBABILITY
        )
        
        val zoneLow = zoneHigh.copy(demandLevel = DemandLevel.LOW)
        
        val savingsHigh = PredictiveAnalyticsEngine.calculateCO2Savings(zoneHigh, 2)
        val savingsLow = PredictiveAnalyticsEngine.calculateCO2Savings(zoneLow, 2)
        
        assertTrue("Savings in high demand area ($savingsHigh) should be greater than low demand ($savingsLow)", 
            savingsHigh > savingsLow)
    }

    @Test
    fun `forecast probability respects limits`() {
        val forecast = PredictiveAnalyticsEngine.forecastProbability(0.9, 12, DemandLevel.EXTREME)
        assertTrue("Forecast should be between 0 and 1", forecast in 0.0..1.0)
    }

    @Test
    fun `area efficiency decreases with cruising pressure`() {
        val analyticsGood = MunicipalAreaAnalytics(
            area = "Good", occupancyRate = 0.7, cruisingPressure = 0.1, 
            illegalParkingRisk = 0.1, avgTimeToParkMinutes = 2, 
            estimatedCO2ImpactKg = 1.0, enforcementPriority = "Low", 
            privateParkingUtilization = 0.5
        )
        
        val analyticsBad = analyticsGood.copy(cruisingPressure = 0.9)
        
        val efficiencyGood = PredictiveAnalyticsEngine.calculateAreaEfficiency(analyticsGood)
        val efficiencyBad = PredictiveAnalyticsEngine.calculateAreaEfficiency(analyticsBad)
        
        assertTrue("Efficiency for low pressure ($efficiencyGood) should be higher than high pressure ($efficiencyBad)", 
            efficiencyGood > efficiencyBad)
    }
}
