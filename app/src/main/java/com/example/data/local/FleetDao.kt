package com.example.data.local

import androidx.room.*
import com.example.data.FleetContributor
import kotlinx.coroutines.flow.Flow

@Dao
interface FleetDao {
    @Query("SELECT * FROM fleet_contributors")
    fun getAllContributors(): Flow<List<FleetContributor>>

    @Query("SELECT * FROM fleet_contributors WHERE id = :id")
    fun getContributorById(id: String): Flow<FleetContributor?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContributors(contributors: List<FleetContributor>)

    @Update
    suspend fun updateContributor(contributor: FleetContributor)

    @Query("DELETE FROM fleet_contributors")
    suspend fun deleteAll()
}
