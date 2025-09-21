package com.halitbarut.vakit.domain.usecase

import com.halitbarut.vakit.domain.model.DailyPrayerLog
import com.halitbarut.vakit.domain.model.PrayerCount
import com.halitbarut.vakit.domain.model.PrayerStats
import com.halitbarut.vakit.domain.model.PrayerType
import com.halitbarut.vakit.domain.repository.PrayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertFailsWith

class ExportPrayerDataUseCaseTest {

    @Test
    fun `invoke returns csv with ordered prayer rows`() = runTest {
        val repository = FakePrayerRepository(sampleStats())
        val useCase = ExportPrayerDataUseCase(repository)

        val csv = useCase()

        val expected = """
            Prayer,Initial_Debt,Completed,Remaining
            Sabah,20,5,15
            Öğle,15,15,0
            İkindi,10,3,7
            Akşam,25,20,5
            Yatsı,30,10,20
            Vitir,5,2,3
        """.trimIndent()

        assertEquals(expected, csv)
    }

    @Test
    fun `invoke throws when stats are missing`() = runTest {
        val repository = FakePrayerRepository(stats = null)
        val useCase = ExportPrayerDataUseCase(repository)

        assertFailsWith<IllegalStateException> {
            useCase()
        }
    }

    private fun sampleStats(): PrayerStats {
        val counts = listOf(
            PrayerCount(
                type = PrayerType.FAJR,
                debt = 20,
                completed = 5,
                completedToday = 0,
                lastUpdateMillis = 0L,
            ),
            PrayerCount(
                type = PrayerType.DHUHR,
                debt = 15,
                completed = 15,
                completedToday = 0,
                lastUpdateMillis = 0L,
            ),
            PrayerCount(
                type = PrayerType.ASR,
                debt = 10,
                completed = 3,
                completedToday = 0,
                lastUpdateMillis = 0L,
            ),
            PrayerCount(
                type = PrayerType.MAGHRIB,
                debt = 25,
                completed = 20,
                completedToday = 0,
                lastUpdateMillis = 0L,
            ),
            PrayerCount(
                type = PrayerType.ISHA,
                debt = 30,
                completed = 10,
                completedToday = 0,
                lastUpdateMillis = 0L,
            ),
            PrayerCount(
                type = PrayerType.WITR,
                debt = 5,
                completed = 2,
                completedToday = 0,
                lastUpdateMillis = 0L,
            ),
        )

        return PrayerStats(
            startDateMillis = 0L,
            lastDailyResetEpochMillis = 0L,
            prayers = counts,
            isWitrTracked = true,
            bestDailyCompletion = 0,
        )
    }

    private class FakePrayerRepository(
        private val stats: PrayerStats?,
    ) : PrayerRepository {

        override fun observePrayerStats(): Flow<PrayerStats?> = error("Not required")

        override fun observeDailyLogs(startDateEpochDay: Long, endDateEpochDay: Long): Flow<List<DailyPrayerLog>> =
            error("Not required")

        override suspend fun getPrayerStats(): PrayerStats? = stats

        override suspend fun initializePrayerStats(
            debts: Map<PrayerType, Int>,
            isWitrTracked: Boolean,
            nowMillis: Long,
        ) = error("Not required")

        override suspend fun incrementDebt(prayerType: PrayerType) = error("Not required")

        override suspend fun incrementCompleted(prayerType: PrayerType, amount: Int) = error("Not required")

        override suspend fun decrementCompleted(prayerType: PrayerType, amount: Int) = error("Not required")

        override suspend fun manuallyUpdateDebt(prayerType: PrayerType, newDebt: Int) = error("Not required")

        override suspend fun toggleWitrTracking(enabled: Boolean) = error("Not required")

        override suspend fun resetAll() = error("Not required")

        override suspend fun refreshDailyProgress(nowMillis: Long) = error("Not required")
    }
}
