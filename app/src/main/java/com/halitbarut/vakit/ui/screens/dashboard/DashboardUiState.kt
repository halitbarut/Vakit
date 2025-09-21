package com.halitbarut.vakit.ui.screens.dashboard

import com.halitbarut.vakit.domain.model.PrayerType

data class DashboardUiState(
    val isLoading: Boolean = true,
    val totalRemaining: Int = 0,
    val totalCompletedToday: Int = 0,
    val bestDailyCompletion: Int = 0,
    val averageDailyCompletion: Int = 0,
    val estimatedFinishDate: String? = null,
    val prayerCards: List<PrayerCardUiState> = emptyList(),
    val editDialog: EditPrayerDialogUiState? = null,
    val isWitrTracked: Boolean = false,
)

data class PrayerCardUiState(
    val prayerType: PrayerType,
    val displayName: String,
    val remainingCount: Int,
    val remainingText: String,
    val lastUpdateText: String,
    val isIncrementEnabled: Boolean,
    val isDecrementEnabled: Boolean,
    val debt: Int,
    val completed: Int,
)

data class EditPrayerDialogUiState(
    val prayerType: PrayerType,
    val title: String,
    val inputValue: String,
    val errorMessage: String? = null,
) {
    val isSaveEnabled: Boolean get() = inputValue.isNotBlank()
}
