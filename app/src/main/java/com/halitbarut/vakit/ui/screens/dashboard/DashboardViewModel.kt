package com.halitbarut.vakit.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halitbarut.vakit.domain.model.PrayerStats
import com.halitbarut.vakit.domain.model.PrayerType
import com.halitbarut.vakit.domain.usecase.IncrementPrayerCompletionUseCase
import com.halitbarut.vakit.domain.usecase.IncrementPrayerDebtUseCase
import com.halitbarut.vakit.domain.usecase.ManuallyUpdatePrayerDebtUseCase
import com.halitbarut.vakit.domain.usecase.ObservePrayerStatsUseCase
import com.halitbarut.vakit.domain.usecase.RefreshDailyProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.NumberFormat
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val observePrayerStats: ObservePrayerStatsUseCase,
    private val incrementPrayerCompletion: IncrementPrayerCompletionUseCase,
    private val incrementPrayerDebt: IncrementPrayerDebtUseCase,
    private val manuallyUpdatePrayerDebt: ManuallyUpdatePrayerDebtUseCase,
    private val refreshDailyProgress: RefreshDailyProgressUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    private var latestStats: PrayerStats? = null

    init {
        viewModelScope.launch {
            observePrayerStats().collectLatest { stats ->
                if (stats == null) {
                    latestStats = null
                    _uiState.value = DashboardUiState(isLoading = false)
                } else {
                    refreshDailyProgress(clock.millis())
                    latestStats = stats
                    val nextState = stats.toUiState(clock)
                    _uiState.update { current ->
                        nextState.copy(editDialog = current.editDialog)
                    }
                }
            }
        }
    }

    fun onIncreaseDebtClicked(prayerType: PrayerType) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                incrementPrayerDebt(prayerType)
            } finally {
                _uiState.update { state -> state.copy(isLoading = false) }
            }
        }
    }

    fun onCompletedClicked(prayerType: PrayerType) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                incrementPrayerCompletion(prayerType)
            } finally {
                _uiState.update { state -> state.copy(isLoading = false) }
            }
        }
    }

    fun onEditClicked(prayerType: PrayerType) {
        val stats = latestStats ?: return
        if (!stats.isWitrTracked && prayerType == PrayerType.WITR) return
        val prayer = stats.prayers.firstOrNull { it.type == prayerType } ?: return
        _uiState.update { state ->
            state.copy(
                editDialog = EditPrayerDialogUiState(
                    prayerType = prayerType,
                    title = "${prayer.type.displayName} Namazı Borcunu Düzenle",
                    inputValue = prayer.debt.coerceAtLeast(0).toString(),
                    errorMessage = null,
                ),
            )
        }
    }

    fun onEditValueChanged(value: String) {
        _uiState.update { state ->
            val dialog = state.editDialog ?: return@update state
            state.copy(editDialog = dialog.copy(inputValue = value, errorMessage = null))
        }
    }

    fun onEditDismissed() {
        _uiState.update { it.copy(editDialog = null) }
    }

    fun onEditConfirmed() {
        val dialog = _uiState.value.editDialog ?: return
        val sanitizedInput = dialog.inputValue.trim()
        val newDebt = sanitizedInput.toIntOrNull()
        if (newDebt == null || newDebt < 0) {
            _uiState.update { it.copy(editDialog = dialog.copy(errorMessage = "Geçerli bir sayı girin")) }
            return
        }
        viewModelScope.launch {
            manuallyUpdatePrayerDebt(dialog.prayerType, newDebt)
            _uiState.update { it.copy(editDialog = null) }
        }
    }

    private fun PrayerStats.toUiState(clock: Clock): DashboardUiState {
        val nowMillis = clock.millis()
        val daysSinceStart = daysSinceStart(nowMillis).coerceAtLeast(1)
        val averageDaily = if (totalCompleted == 0) {
            0
        } else {
            (totalCompleted / daysSinceStart.toDouble()).toInt().coerceAtLeast(0)
        }
        val estimatedFinishDate = if (averageDaily > 0) {
            val remainingDays = (totalRemaining / averageDaily.toDouble()).let { kotlin.math.ceil(it) }.toLong()
            val estimatedDate = Instant.ofEpochMilli(nowMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                .plusDays(remainingDays)
            estimatedDateFormatter.format(estimatedDate)
        } else {
            null
        }
        val prayerCards = prayers.filter { prayerCount ->
            isWitrTracked || prayerCount.type != PrayerType.WITR
        }.map { prayerCount ->
            PrayerCardUiState(
                prayerType = prayerCount.type,
                displayName = prayerCount.type.displayName,
                remainingCount = prayerCount.remaining,
                remainingText = formatCount(prayerCount.remaining),
                lastUpdateText = formatLastUpdate(prayerCount.lastUpdateMillis),
                isIncrementEnabled = prayerCount.remaining > 0,
                isDecrementEnabled = prayerCount.completed > 0,
                debt = prayerCount.debt,
                completed = prayerCount.completed,
            )
        }
        return DashboardUiState(
            isLoading = false,
            totalRemaining = totalRemaining,
            totalCompletedToday = totalCompletedToday,
            bestDailyCompletion = bestDailyCompletion,
            averageDailyCompletion = averageDaily,
            estimatedFinishDate = estimatedFinishDate,
            prayerCards = prayerCards,
            isWitrTracked = isWitrTracked,
        )
    }

    private fun formatLastUpdate(millis: Long): String =
        if (millis <= 0L) {
            "Henüz güncellenmedi"
        } else {
            val zonedDateTime = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
            "Son Güncelleme: ${lastUpdateFormatter.format(zonedDateTime)}"
        }

    private fun formatCount(value: Int): String =
        numberFormatter.format(value.coerceAtLeast(0))

    private fun Clock.millis(): Long = instant().toEpochMilli()

    companion object {
        private val estimatedDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("tr"))
        private val lastUpdateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale("tr"))
        private val numberFormatter: NumberFormat = NumberFormat.getIntegerInstance(Locale("tr"))
    }
}
