package com.harsh.healthmonitor.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HealthDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: HealthRecord)

    @Query("SELECT * FROM health_records ORDER BY timestamp DESC")
    suspend fun getAllRecords(): List<HealthRecord>

    @Query("SELECT * FROM health_records ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestRecord(): HealthRecord?
}
