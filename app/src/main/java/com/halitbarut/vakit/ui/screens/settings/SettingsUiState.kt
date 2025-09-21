package com.halitbarut.vakit.ui.screens.settings

import com.halitbarut.vakit.data.repository.UserPreferencesRepository

data class SettingsUiState(
    val isLoading: Boolean = true,
    val isExporting: Boolean = false,
    val isWitrTracked: Boolean = false,
    val notificationsEnabled: Boolean = false,
    val notificationTime: String = UserPreferencesRepository.DEFAULT_NOTIFICATION_TIME,
    val showResetConfirmation: Boolean = false,
)
