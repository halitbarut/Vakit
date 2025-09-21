package com.halitbarut.vakit.ui.screens.onboarding

import com.halitbarut.vakit.domain.model.PrayerType

data class OnboardingUiState(
    val prayerInputs: List<PrayerDebtInputState> = PrayerType.ordered.map { PrayerDebtInputState(it) },
    val isWitrTracked: Boolean = false,
    val isLoading: Boolean = false,
    val showHelper: Boolean = false,
    val errorMessage: String? = null,
)

data class PrayerDebtInputState(
    val prayerType: PrayerType,
    val years: String = "",
    val months: String = "",
    val days: String = "",
)

sealed interface OnboardingEvent {
    data object NavigateToDashboard : OnboardingEvent
}
