package com.example.data.local

import androidx.room.*
import com.example.data.ParkingZone
import kotlinx.coroutines.flow.Flow

@Dao
interface ZoneDao {
    @Query("SELECT * FROM parking_zones")
    fun getAllZones(): Flow<List<ParkingZone>>

    @Query("SELECT * FROM parking_zones WHERE id = :id")
    fun getZoneById(id: String): Flow<ParkingZone?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZones(zones: List<ParkingZone>)

    @Update
    suspend fun updateZone(zone: ParkingZone)

    @Query("DELETE FROM parking_zones")
    suspend fun deleteAll()
}
