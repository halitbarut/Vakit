package com.halitbarut.vakit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_logs")
data class DailyLogEntity(
    @PrimaryKey
    val dateEpochDay: Long,
    val fajrCompleted: Int,
    val dhuhrCompleted: Int,
    val asrCompleted: Int,
    val maghribCompleted: Int,
    val ishaCompleted: Int,
    val witrCompleted: Int,
)
