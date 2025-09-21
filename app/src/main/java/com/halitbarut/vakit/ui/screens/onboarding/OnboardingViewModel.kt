package com.halitbarut.vakit.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halitbarut.vakit.domain.model.PrayerType
import com.halitbarut.vakit.domain.usecase.GetPrayerStatsUseCase
import com.halitbarut.vakit.domain.usecase.InitializePrayerStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val getPrayerStatsUseCase: GetPrayerStatsUseCase,
    private val initializePrayerStatsUseCase: InitializePrayerStatsUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<OnboardingEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val stats = getPrayerStatsUseCase()
            if (stats != null) {
                _events.emit(OnboardingEvent.NavigateToDashboard)
            }
        }
    }

    fun updateYears(prayerType: PrayerType, value: String) {
        updateInput(prayerType) { it.copy(years = value.onlyDigits()) }
    }

    fun updateMonths(prayerType: PrayerType, value: String) {
        updateInput(prayerType) { it.copy(months = value.onlyDigits()) }
    }

    fun updateDays(prayerType: PrayerType, value: String) {
        updateInput(prayerType) { it.copy(days = value.onlyDigits()) }
    }

    fun toggleWitrTracking(enabled: Boolean) {
        _uiState.update { state -> state.copy(isWitrTracked = enabled) }
    }

    fun toggleHelperSheet() {
        _uiState.update { state -> state.copy(showHelper = !state.showHelper) }
    }

    fun startTracking() {
        viewModelScope.launch {
            val current = _uiState.value
            if (current.isLoading) return@launch
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val debts = current.prayerInputs.associate { input ->
                    input.prayerType to input.totalDebt()
                }.toMutableMap()
                if (!current.isWitrTracked) {
                    debts[PrayerType.WITR] = 0
                }
                initializePrayerStatsUseCase(
                    debts = debts,
                    isWitrTracked = current.isWitrTracked,
                    nowMillis = clock.millis(),
                )
            }.onSuccess {
                _events.emit(OnboardingEvent.NavigateToDashboard)
                _uiState.update { it.copy(isLoading = false) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Beklenmeyen bir hata oluştu",
                    )
                }
            }
        }
    }

    private fun updateInput(prayerType: PrayerType, transform: (PrayerDebtInputState) -> PrayerDebtInputState) {
        _uiState.update { state ->
            val updatedInputs = state.prayerInputs.map { input ->
                if (input.prayerType == prayerType) transform(input) else input
            }
            state.copy(prayerInputs = updatedInputs)
        }
    }

    private fun String.onlyDigits(): String = filter { it.isDigit() }.take(MAX_DIGITS)

    private fun Clock.millis(): Long = instant().toEpochMilli()

    private fun PrayerDebtInputState.totalDebt(): Int {
        val yearsValue = years.toIntOrNull() ?: 0
        val monthsValue = months.toIntOrNull() ?: 0
        val daysValue = days.toIntOrNull() ?: 0
        val yearContribution = yearsValue * DAYS_IN_YEAR
        val monthContribution = monthsValue * DAYS_IN_MONTH
        val total = yearContribution + monthContribution + daysValue
        return total.coerceAtLeast(0)
    }

    companion object {
        private const val DAYS_IN_YEAR = 365
        private const val DAYS_IN_MONTH = 30
        private const val MAX_DIGITS = 4
    }
}
