package com.halitbarut.vakit.domain.model

data class DailyPrayerLog(
    val dateEpochDay: Long,
    val fajrCompleted: Int,
    val dhuhrCompleted: Int,
    val asrCompleted: Int,
    val maghribCompleted: Int,
    val ishaCompleted: Int,
    val witrCompleted: Int,
) {
    val totalCompleted: Int =
        fajrCompleted + dhuhrCompleted + asrCompleted + maghribCompleted + ishaCompleted + witrCompleted
}
