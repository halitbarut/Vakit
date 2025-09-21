package com.halitbarut.vakit.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayer_stats")
data class PrayerEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val startDateMillis: Long,
    val fajrDebt: Int,
    val fajrCompleted: Int,
    val fajrCompletedToday: Int,
    @ColumnInfo(name = "fajr_last_update_millis") val fajrLastUpdateMillis: Long,
    val dhuhrDebt: Int,
    val dhuhrCompleted: Int,
    val dhuhrCompletedToday: Int,
    @ColumnInfo(name = "dhuhr_last_update_millis") val dhuhrLastUpdateMillis: Long,
    val asrDebt: Int,
    val asrCompleted: Int,
    val asrCompletedToday: Int,
    @ColumnInfo(name = "asr_last_update_millis") val asrLastUpdateMillis: Long,
    val maghribDebt: Int,
    val maghribCompleted: Int,
    val maghribCompletedToday: Int,
    @ColumnInfo(name = "maghrib_last_update_millis") val maghribLastUpdateMillis: Long,
    val ishaDebt: Int,
    val ishaCompleted: Int,
    val ishaCompletedToday: Int,
    @ColumnInfo(name = "isha_last_update_millis") val ishaLastUpdateMillis: Long,
    val witrDebt: Int,
    val witrCompleted: Int,
    val witrCompletedToday: Int,
    @ColumnInfo(name = "witr_last_update_millis") val witrLastUpdateMillis: Long,
    val isWitrTracked: Boolean,
    val lastDailyResetEpochMillis: Long,
    val bestDailyCompletion: Int,
) {
    fun totalCompletedToday(): Int =
        fajrCompletedToday + dhuhrCompletedToday + asrCompletedToday +
            maghribCompletedToday + ishaCompletedToday + if (isWitrTracked) witrCompletedToday else 0

    fun totalCompleted(): Int =
        fajrCompleted + dhuhrCompleted + asrCompleted + maghribCompleted + ishaCompleted +
            if (isWitrTracked) witrCompleted else 0

    fun totalDebtRemaining(): Int = listOf(
        fajrDebt - fajrCompleted,
        dhuhrDebt - dhuhrCompleted,
        asrDebt - asrCompleted,
        maghribDebt - maghribCompleted,
        ishaDebt - ishaCompleted,
        if (isWitrTracked) witrDebt - witrCompleted else 0,
    ).sumOf { it.coerceAtLeast(0) }

    companion object {
        const val SINGLETON_ID: Int = 0
    }
}
