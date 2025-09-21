package com.halitbarut.vakit.domain.model

data class PrayerCount(
    val type: PrayerType,
    val debt: Int,
    val completed: Int,
    val completedToday: Int,
    val lastUpdateMillis: Long,
) {
    val remaining: Int get() = (debt - completed).coerceAtLeast(0)
}
