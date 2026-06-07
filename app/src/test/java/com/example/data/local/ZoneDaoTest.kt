package com.example.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ZoneDaoTest {
    private lateinit var db: ParkingDatabase
    private lateinit var dao: ZoneDao

    private val testZone = ParkingZone(
        id = "test_z1",
        name = "Test Zone",
        area = "Test Area",
        mockH3Index = "h3",
        latitude = 37.0,
        longitude = 23.0,
        probability = 0.5,
        confidence = 1.0,
        freshnessMinutes = 0,
        expectedTimeToParkMinutes = 5,
        walkingTimeToDestinationMinutes = 5,
        congestionImpact = 0.1,
        legalRisk = LegalRisk.LOW,
        demandLevel = DemandLevel.MEDIUM,
        supplySignal = 0.5,
        lastUpdatedLabel = "Just now",
        status = ZoneStatus.MEDIUM_PROBABILITY,
        competitionScore = 0.0,
        activeUsers = 0
    )

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ParkingDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.zoneDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `insert and get zones`() = runBlocking {
        dao.insertZones(listOf(testZone))
        val zones = dao.getAllZones().first()
        assertEquals(1, zones.size)
        assertEquals("test_z1", zones[0].id)
    }

    @Test
    fun `update zone status and observe flow`() = runBlocking {
        dao.insertZones(listOf(testZone))
        val updatedZone = testZone.copy(probability = 0.9, status = ZoneStatus.HIGH_PROBABILITY)
        dao.updateZone(updatedZone)
        
        val zones = dao.getAllZones().first()
        assertEquals(0.9, zones[0].probability, 0.01)
        assertEquals(ZoneStatus.HIGH_PROBABILITY, zones[0].status)
    }
}
