package com.harsh.healthmonitor.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_records")
data class HealthRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val steps: Long,
    val heartRate: Double,
    val calories: Double
)
