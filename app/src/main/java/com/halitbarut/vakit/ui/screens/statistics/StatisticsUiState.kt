package com.halitbarut.vakit.ui.screens.statistics

import com.halitbarut.vakit.domain.model.PrayerType

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val hasData: Boolean = false,
    val weeklyPerformance: List<WeeklyPerformanceBar> = emptyList(),
    val averageDailyCompletion: Int = 0,
    val bestDailyCompletion: Int = 0,
    val totalPrayed: Int = 0,
    val distribution: List<PrayerDistribution> = emptyList(),
    val isWitrTracked: Boolean = false,
)

data class WeeklyPerformanceBar(
    val dayLabel: String,
    val totalCompleted: Int,
)

data class PrayerDistribution(
    val prayerType: PrayerType,
    val completed: Int,
    val debt: Int,
)
