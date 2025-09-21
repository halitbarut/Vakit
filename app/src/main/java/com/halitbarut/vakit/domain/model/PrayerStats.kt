package com.halitbarut.vakit.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class PrayerStats(
    val startDateMillis: Long,
    val lastDailyResetEpochMillis: Long,
    val prayers: List<PrayerCount>,
    val isWitrTracked: Boolean,
    val bestDailyCompletion: Int,
) {
    val startDate: LocalDate get() = Instant.ofEpochMilli(startDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    val totalRemaining: Int get() = prayers.filter { isWitrTracked || it.type != PrayerType.WITR }.sumOf { it.remaining }
    val totalCompleted: Int get() = prayers.filter { isWitrTracked || it.type != PrayerType.WITR }.sumOf { it.completed }
    val totalCompletedToday: Int
        get() = prayers.filter { isWitrTracked || it.type != PrayerType.WITR }.sumOf { it.completedToday }

    fun daysSinceStart(nowMillis: Long): Long {
        val today = Instant.ofEpochMilli(nowMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        val delta = java.time.Period.between(startDate, today)
        val days = delta.years * 365L + delta.months * 30L + delta.days
        return days.coerceAtLeast(1)
    }
}
