package com.halitbarut.vakit.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halitbarut.vakit.data.repository.UserPreferencesRepository
import com.halitbarut.vakit.domain.usecase.ObservePrayerStatsUseCase
import com.halitbarut.vakit.domain.usecase.ResetPrayerStatsUseCase
import com.halitbarut.vakit.domain.usecase.ToggleWitrTrackingUseCase
import com.halitbarut.vakit.notifications.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val observePrayerStatsUseCase: ObservePrayerStatsUseCase,
    private val toggleWitrTrackingUseCase: ToggleWitrTrackingUseCase,
    private val resetPrayerStatsUseCase: ResetPrayerStatsUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val notificationScheduler: NotificationScheduler,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    private var hasInitializedScheduler = false

    init {
        viewModelScope.launch {
            observePrayerStatsUseCase()
                .combine(userPreferencesRepository.preferencesFlow) { stats, prefs -> stats to prefs }
                .collectLatest { (stats, prefs) ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isWitrTracked = stats?.isWitrTracked ?: false,
                            notificationsEnabled = prefs.isEnabled,
                            notificationTime = prefs.time,
                        )
                    }

                    if (!hasInitializedScheduler) {
                        hasInitializedScheduler = true
                        if (prefs.isEnabled) {
                            val (hour, minute) = parseTime(prefs.time)
                            notificationScheduler.scheduleDailyReminder(hour, minute)
                        }
                    }
                }
        }
    }

    fun onToggleWitr(enabled: Boolean) {
        viewModelScope.launch {
            toggleWitrTrackingUseCase(enabled)
        }
        _uiState.update { it.copy(isWitrTracked = enabled) }
    }

    fun onToggleNotifications(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled) }
        viewModelScope.launch {
            userPreferencesRepository.setNotificationsEnabled(enabled)
            val (hour, minute) = parseTime(_uiState.value.notificationTime)
            if (enabled) {
                notificationScheduler.scheduleDailyReminder(hour, minute)
            } else {
                notificationScheduler.cancelDailyReminder()
            }
        }
    }

    fun onNotificationTimeSelected(hour: Int, minute: Int) {
        val formatted = String.format("%02d:%02d", hour, minute)
        _uiState.update { it.copy(notificationTime = formatted) }
        viewModelScope.launch {
            userPreferencesRepository.setNotificationTime(formatted)
            if (_uiState.value.notificationsEnabled) {
                notificationScheduler.scheduleDailyReminder(hour, minute)
            }
        }
    }

    fun onResetAllConfirmed() {
        viewModelScope.launch {
            resetPrayerStatsUseCase()
        }
        _uiState.update { it.copy(showResetConfirmation = false) }
    }

    fun onResetClicked() {
        _uiState.update { it.copy(showResetConfirmation = true) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(showResetConfirmation = false) }
    }

    private fun parseTime(time: String): Pair<Int, Int> {
        val parts = time.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 20
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 30
        return hour to minute
    }
}
