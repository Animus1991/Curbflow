package com.example.data.local

import androidx.room.*
import com.example.data.H3CellEntity

@Dao
interface H3CellDao {
    @Query("SELECT * FROM h3_cells WHERE expiresAt > :now ORDER BY probability DESC")
    suspend fun getFreshCells(now: Long = System.currentTimeMillis()): List<H3CellEntity>

    @Query("SELECT * FROM h3_cells WHERE h3Index = :h3Index")
    suspend fun getCell(h3Index: String): H3CellEntity?

    @Upsert
    suspend fun upsert(cell: H3CellEntity)

    @Upsert
    suspend fun upsertAll(cells: List<H3CellEntity>)

    @Query("DELETE FROM h3_cells WHERE expiresAt < :cutoffTime")
    suspend fun deleteExpired(cutoffTime: Long = System.currentTimeMillis())

    @Query("DELETE FROM h3_cells")
    suspend fun deleteAll()
}
