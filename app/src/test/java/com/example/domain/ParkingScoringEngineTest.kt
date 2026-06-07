package com.example.domain

import com.example.data.*
import org.junit.Assert.*
import org.junit.Test

class ParkingScoringEngineTest {

    private val baseZone = ParkingZone(
        id = "test",
        name = "Test Zone",
        area = "Test Area",
        mockH3Index = "h3",
        latitude = 0.0,
        longitude = 0.0,
        probability = 0.8,
        confidence = 1.0,
        freshnessMinutes = 0,
        expectedTimeToParkMinutes = 2,
        walkingTimeToDestinationMinutes = 2,
        congestionImpact = 0.1,
        legalRisk = LegalRisk.LOW,
        demandLevel = DemandLevel.MEDIUM,
        supplySignal = 0.8,
        lastUpdatedLabel = "Just now",
        status = ZoneStatus.HIGH_PROBABILITY,
        competitionScore = 0.0,
        activeUsers = 0
    )

    @Test
    fun `score decreases as active users increase`() {
        val lowPressure = baseZone.copy(activeUsers = 0)
        val highPressure = baseZone.copy(activeUsers = 5)

        val scoreLow = ParkingScoringEngine.calculateParkingOpportunityScore(lowPressure)
        val scoreHigh = ParkingScoringEngine.calculateParkingOpportunityScore(highPressure)

        assertTrue("Score with high pressure ($scoreHigh) should be lower than low pressure ($scoreLow)", scoreHigh < scoreLow)
    }

    @Test
    fun `score decreases exponentially with freshness in extreme demand`() {
        val freshZone = baseZone.copy(demandLevel = DemandLevel.EXTREME, freshnessMinutes = 0)
        val staleZone = baseZone.copy(demandLevel = DemandLevel.EXTREME, freshnessMinutes = 10)

        val scoreFresh = ParkingScoringEngine.calculateParkingOpportunityScore(freshZone)
        val scoreStale = ParkingScoringEngine.calculateParkingOpportunityScore(staleZone)

        assertTrue("Stale zone in extreme demand should have significantly lower score", scoreStale < scoreFresh * 0.5)
    }

    @Test
    fun `feedback FOUND_PARKING resets freshness and increases probability`() {
        val lowProbZone = baseZone.copy(probability = 0.2, freshnessMinutes = 20)
        val updated = ParkingScoringEngine.updateZoneAfterUserFeedback(lowProbZone, FeedbackType.FOUND_PARKING)

        assertEquals("Freshness should be reset to 0", 0, updated.freshnessMinutes)
        assertTrue("Probability should increase", updated.probability > 0.2)
        assertEquals("Confidence should be reset to 1.0", 1.0, updated.confidence, 0.01)
    }

    @Test
    fun `RESTRICTED legal risk results in zero or near-zero score`() {
        val restrictedZone = baseZone.copy(legalRisk = LegalRisk.RESTRICTED)
        val score = ParkingScoringEngine.calculateParkingOpportunityScore(restrictedZone)

        assertEquals("Restricted zone should have 0 score", 0.0, score, 0.05)
    }
}
