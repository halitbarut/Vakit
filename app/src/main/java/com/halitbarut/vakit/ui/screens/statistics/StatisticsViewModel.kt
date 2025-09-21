package com.halitbarut.vakit.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halitbarut.vakit.domain.model.DailyPrayerLog
import com.halitbarut.vakit.domain.model.PrayerStats
import com.halitbarut.vakit.domain.model.PrayerType
import com.halitbarut.vakit.domain.usecase.ObserveDailyLogsUseCase
import com.halitbarut.vakit.domain.usecase.ObservePrayerStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    observeDailyLogsUseCase: ObserveDailyLogsUseCase,
    private val observePrayerStatsUseCase: ObservePrayerStatsUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState

    private val locale = Locale("tr", "TR")
    private val today: LocalDate get() = LocalDate.now(clock)
    private val startEpochDay: Long
    private val endEpochDay: Long

    init {
        val end = today.toEpochDay()
        val start = today.minusDays(6).toEpochDay()
        startEpochDay = start
        endEpochDay = end

        val dailyLogsFlow = observeDailyLogsUseCase(startEpochDay, endEpochDay)

        viewModelScope.launch {
            combine(
                dailyLogsFlow,
                observePrayerStatsUseCase(),
            ) { logs, stats -> logs to stats }
                .collectLatest { (logs, stats) ->
                    _uiState.update {
                        buildUiState(logs, stats)
                    }
                }
        }
    }

    private fun buildUiState(logs: List<DailyPrayerLog>, stats: PrayerStats?): StatisticsUiState {
        val isWitrTracked = stats?.isWitrTracked ?: true
        val weeklyPerformance = buildWeeklyPerformance(logs, isWitrTracked)
        val weeklyTotal = weeklyPerformance.sumOf { it.totalCompleted }
        val bestDaily = weeklyPerformance.maxOfOrNull { it.totalCompleted } ?: 0
        val averageDaily = if (weeklyPerformance.isNotEmpty()) {
            (weeklyTotal / weeklyPerformance.size.toDouble()).roundToInt()
        } else {
            0
        }

        val distribution = buildDistribution(stats, logs, isWitrTracked)
        val hasData = weeklyTotal > 0 || distribution.any { it.completed > 0 }

        return StatisticsUiState(
            isLoading = false,
            hasData = hasData,
            weeklyPerformance = weeklyPerformance,
            averageDailyCompletion = averageDaily,
            bestDailyCompletion = bestDaily,
            totalPrayed = weeklyTotal,
            distribution = distribution,
            isWitrTracked = isWitrTracked,
        )
    }

    private fun buildWeeklyPerformance(
        logs: List<DailyPrayerLog>,
        isWitrTracked: Boolean,
    ): List<WeeklyPerformanceBar> {
        val logByDate = logs.associateBy { it.dateEpochDay }
        return (startEpochDay..endEpochDay).map { epochDay ->
            val date = LocalDate.ofEpochDay(epochDay)
            val total = logByDate[epochDay]?.totalForTracking(isWitrTracked) ?: 0
            WeeklyPerformanceBar(
                dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale).capitalizeTr(),
                totalCompleted = total,
            )
        }
    }

    private fun buildDistribution(
        stats: PrayerStats?,
        logs: List<DailyPrayerLog>,
        isWitrTracked: Boolean,
    ): List<PrayerDistribution> {
        return if (stats != null) {
            stats.prayers
                .filter { isWitrTracked || it.type != PrayerType.WITR }
                .map {
                    PrayerDistribution(
                        prayerType = it.type,
                        completed = it.completed,
                        debt = it.debt,
                    )
                }
        } else {
            if (logs.isEmpty()) {
                emptyList()
            } else {
                PrayerType.ordered
                    .filter { isWitrTracked || it != PrayerType.WITR }
                    .map { type ->
                        PrayerDistribution(
                            prayerType = type,
                            completed = logs.sumOf { it.completedFor(type) },
                            debt = 0,
                        )
                    }
            }
        }
    }

    private fun DailyPrayerLog.totalForTracking(isWitrTracked: Boolean): Int =
        if (isWitrTracked) totalCompleted else totalCompleted - witrCompleted

    private fun DailyPrayerLog.completedFor(prayerType: PrayerType): Int = when (prayerType) {
        PrayerType.FAJR -> fajrCompleted
        PrayerType.DHUHR -> dhuhrCompleted
        PrayerType.ASR -> asrCompleted
        PrayerType.MAGHRIB -> maghribCompleted
        PrayerType.ISHA -> ishaCompleted
        PrayerType.WITR -> witrCompleted
    }

    private fun String.capitalizeTr(): String = replaceFirstChar { ch ->
        if (ch.isLowerCase()) ch.titlecase(locale) else ch.toString()
    }
}
