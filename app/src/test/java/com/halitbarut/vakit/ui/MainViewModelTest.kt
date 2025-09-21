package com.halitbarut.vakit.ui

import com.halitbarut.vakit.MainDispatcherRule
import com.halitbarut.vakit.domain.model.PrayerCount
import com.halitbarut.vakit.domain.model.PrayerStats
import com.halitbarut.vakit.domain.model.PrayerType
import com.halitbarut.vakit.domain.repository.PrayerRepository
import com.halitbarut.vakit.navigation.VakitDestination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun onboardingSelected_whenNoStoredStats() {
        val viewModel = MainViewModel(
            prayerRepository = FakePrayerRepository(hasStats = false),
        )

        dispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals(VakitDestination.Onboarding.route, uiState.startDestination)
    }

    @Test
    fun dashboardSelected_whenStatsExist() {
        val viewModel = MainViewModel(
            prayerRepository = FakePrayerRepository(hasStats = true),
        )

        dispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals(VakitDestination.Dashboard.route, uiState.startDestination)
    }

    private class FakePrayerRepository(
        private val hasStats: Boolean,
    ) : PrayerRepository {

        override fun observePrayerStats(): Flow<PrayerStats?> = flowOf(statsOrNull())

        override suspend fun getPrayerStats(): PrayerStats? = statsOrNull()

        override suspend fun initializePrayerStats(
            debts: Map<PrayerType, Int>,
            isWitrTracked: Boolean,
            nowMillis: Long,
        ) = Unit

        override suspend fun incrementCompleted(prayerType: PrayerType, amount: Int) = Unit

        override suspend fun incrementDebt(prayerType: PrayerType) = Unit

        override suspend fun decrementCompleted(prayerType: PrayerType, amount: Int) = Unit

        override suspend fun manuallyUpdateDebt(prayerType: PrayerType, newDebt: Int) = Unit

        override suspend fun toggleWitrTracking(enabled: Boolean) = Unit

        override suspend fun resetAll() = Unit

        override suspend fun refreshDailyProgress(nowMillis: Long) = Unit

        private fun statsOrNull(): PrayerStats? =
            if (hasStats) {
                PrayerStats(
                    startDateMillis = 0L,
                    lastDailyResetEpochMillis = 0L,
                    prayers = PrayerType.ordered.map { type ->
                        PrayerCount(
                            type = type,
                            debt = 0,
                            completed = 0,
                            completedToday = 0,
                            lastUpdateMillis = 0L,
                        )
                    },
                    isWitrTracked = true,
                    bestDailyCompletion = 0,
                )
            } else {
                null
            }
    }
}
