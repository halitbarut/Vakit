package com.halitbarut.vakit.data.repository

import com.halitbarut.vakit.data.local.dao.DailyLogDao
import com.halitbarut.vakit.data.local.dao.PrayerDao
import com.halitbarut.vakit.data.local.entity.DailyLogEntity
import com.halitbarut.vakit.data.local.entity.PrayerEntity
import com.halitbarut.vakit.domain.model.PrayerType
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class PrayerRepositoryImplTest {

    private lateinit var repository: PrayerRepositoryImpl
    private lateinit var fakeDao: FakePrayerDao
    private lateinit var fakeDailyLogDao: FakeDailyLogDao
    private val testDispatcher = StandardTestDispatcher()
    private val clock: Clock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC"))

    @Before
    fun setup() {
        fakeDao = FakePrayerDao()
        fakeDailyLogDao = FakeDailyLogDao()
        repository = PrayerRepositoryImpl(
            prayerDao = fakeDao,
            dailyLogDao = fakeDailyLogDao,
            clock = clock,
            ioDispatcher = testDispatcher,
        )
    }

    @Test
    fun initializePrayerStats_persistsDebts_andSkipsWitrWhenDisabled() = runTest(testDispatcher) {
        val now = clock.instant().toEpochMilli()
        val debts = mapOf(
            PrayerType.FAJR to 120,
            PrayerType.DHUHR to 95,
            PrayerType.ASR to 80,
            PrayerType.MAGHRIB to 70,
            PrayerType.ISHA to 60,
            PrayerType.WITR to 40,
        )

        repository.initializePrayerStats(
            debts = debts,
            isWitrTracked = false,
            nowMillis = now,
        )

        val stats = repository.observePrayerStats().first()
        assertNotNull(stats)
        stats!!
        assertEquals(120, stats.prayers.first { it.type == PrayerType.FAJR }.debt)
        assertEquals(0, stats.prayers.first { it.type == PrayerType.WITR }.debt)
        assertFalse(stats.isWitrTracked)
        assertEquals(now, stats.prayers.first { it.type == PrayerType.FAJR }.lastUpdateMillis)
    }

    @Test
    fun incrementCompleted_updatesCompletionCounters_only() = runTest(testDispatcher) {
        val now = clock.instant().toEpochMilli()
        repository.initializePrayerStats(
            debts = PrayerType.ordered.associateWith { 5 },
            isWitrTracked = true,
            nowMillis = now,
        )

        repository.incrementCompleted(PrayerType.FAJR)
        repository.incrementCompleted(PrayerType.FAJR)

        val stats = repository.getPrayerStats()
        assertNotNull(stats)
        stats!!
        val fajr = stats.prayers.first { it.type == PrayerType.FAJR }
        assertEquals(5, fajr.debt)
        assertEquals(2, fajr.completed)
        assertEquals(2, fajr.completedToday)
        assertEquals(3, fajr.remaining)
        assertEquals(now, fajr.lastUpdateMillis)

        val log = fakeDailyLogDao.lastUpserted
        assertNotNull(log)
        assertEquals(2, log!!.fajrCompleted)
    }

    @Test
    fun incrementDebt_increasesDebtWithoutLimit_andUpdatesTimestamp() = runTest(testDispatcher) {
        val now = clock.instant().toEpochMilli()
        repository.initializePrayerStats(
            debts = PrayerType.ordered.associateWith { 2 },
            isWitrTracked = true,
            nowMillis = now,
        )

        repository.incrementDebt(PrayerType.FAJR)
        repository.incrementDebt(PrayerType.FAJR)

        val stats = repository.getPrayerStats()
        assertNotNull(stats)
        val fajr = stats!!.prayers.first { it.type == PrayerType.FAJR }
        assertEquals(4, fajr.debt)
        assertEquals(now, fajr.lastUpdateMillis)
    }

    @Test
    fun decrementCompleted_reducesCounters_andUpdatesTimestamp() = runTest(testDispatcher) {
        val now = clock.instant().toEpochMilli()
        repository.initializePrayerStats(
            debts = PrayerType.ordered.associateWith { 5 },
            isWitrTracked = true,
            nowMillis = now,
        )

        repository.incrementCompleted(PrayerType.FAJR, amount = 3)

        repository.decrementCompleted(PrayerType.FAJR)

        val stats = repository.getPrayerStats()
        assertNotNull(stats)
        val fajr = stats!!.prayers.first { it.type == PrayerType.FAJR }
        assertEquals(2, fajr.completed)
        assertEquals(2, fajr.completedToday)
        assertEquals(now, fajr.lastUpdateMillis)

        val log = fakeDailyLogDao.lastUpserted
        assertNotNull(log)
        assertEquals(2, log!!.fajrCompleted)
    }

    @Test
    fun manuallyUpdateDebt_updatesDebt_andTimestamp() = runTest(testDispatcher) {
        val now = clock.instant().toEpochMilli()
        repository.initializePrayerStats(
            debts = PrayerType.ordered.associateWith { 5 },
            isWitrTracked = true,
            nowMillis = now,
        )

        fakeDao.upsert(
            fakeDao.current()!!.copy(
                asrDebt = 8,
                asrLastUpdateMillis = 0,
            ),
        )

        repository.manuallyUpdateDebt(PrayerType.ASR, newDebt = 15)

        val stats = repository.getPrayerStats()
        assertNotNull(stats)
        val asr = stats!!.prayers.first { it.type == PrayerType.ASR }
        assertEquals(15, asr.debt)
        assertEquals(now, asr.lastUpdateMillis)
    }

    @Test
    fun manuallyUpdateDebt_usesEnteredRemainingValue_whenCompletionsExist() = runTest(testDispatcher) {
        val now = clock.instant().toEpochMilli()
        repository.initializePrayerStats(
            debts = PrayerType.ordered.associateWith { 50 },
            isWitrTracked = true,
            nowMillis = now,
        )

        fakeDao.upsert(
            fakeDao.current()!!.copy(
                dhuhrDebt = 50,
                dhuhrCompleted = 15,
                dhuhrCompletedToday = 4,
                dhuhrLastUpdateMillis = 0,
            ),
        )

        repository.manuallyUpdateDebt(PrayerType.DHUHR, newDebt = 67)

        val stats = repository.getPrayerStats()
        assertNotNull(stats)
        val dhuhr = stats!!.prayers.first { it.type == PrayerType.DHUHR }
        assertEquals(15, dhuhr.completed)
        assertEquals(82, dhuhr.debt)
        assertEquals(67, dhuhr.remaining)
        assertEquals(now, dhuhr.lastUpdateMillis)
    }

    @Test
    fun refreshDailyProgress_resetsDailyCounters_andUpdatesBest() = runTest(testDispatcher) {
        val now = clock.instant().toEpochMilli()
        val entity = PrayerEntity(
            startDateMillis = now,
            fajrDebt = 10,
            fajrCompleted = 0,
            fajrCompletedToday = 5,
            fajrLastUpdateMillis = now,
            dhuhrDebt = 10,
            dhuhrCompleted = 0,
            dhuhrCompletedToday = 0,
            dhuhrLastUpdateMillis = now,
            asrDebt = 10,
            asrCompleted = 0,
            asrCompletedToday = 0,
            asrLastUpdateMillis = now,
            maghribDebt = 10,
            maghribCompleted = 0,
            maghribCompletedToday = 0,
            maghribLastUpdateMillis = now,
            ishaDebt = 10,
            ishaCompleted = 0,
            ishaCompletedToday = 0,
            ishaLastUpdateMillis = now,
            witrDebt = 10,
            witrCompleted = 0,
            witrCompletedToday = 0,
            witrLastUpdateMillis = now,
            isWitrTracked = true,
            lastDailyResetEpochMillis = now,
            bestDailyCompletion = 3,
        )
        fakeDao.upsert(entity)

        val tomorrow = now + 24 * 60 * 60 * 1000
        repository.refreshDailyProgress(nowMillis = tomorrow)

        val refreshed = fakeDao.current()
        assertNotNull(refreshed)
        refreshed!!
        assertEquals(0, refreshed.fajrCompletedToday)
        assertEquals(5, refreshed.bestDailyCompletion)
        assertEquals(tomorrow, refreshed.lastDailyResetEpochMillis)
    }

    private class FakePrayerDao : PrayerDao {
        private val state = MutableStateFlow<PrayerEntity?>(null)

        override fun observePrayerStats(id: Int): Flow<PrayerEntity?> = state

        override suspend fun getPrayerStats(id: Int): PrayerEntity? = state.value

        override suspend fun upsert(prayerEntity: PrayerEntity) {
            state.value = prayerEntity
        }

        override suspend fun clearAll() {
            state.value = null
        }

        fun current(): PrayerEntity? = state.value
    }

    private class FakeDailyLogDao : DailyLogDao {
        private val logs = MutableStateFlow<Map<Long, DailyLogEntity>>(emptyMap())
        var lastUpserted: DailyLogEntity? = null
            private set

        override suspend fun upsert(log: DailyLogEntity) {
            lastUpserted = log
            logs.value = logs.value + (log.dateEpochDay to log)
        }

        override suspend fun getByDate(date: Long): DailyLogEntity? = logs.value[date]

        override fun getLogsForDateRange(startDate: Long, endDate: Long): Flow<List<DailyLogEntity>> =
            logs.map { entries ->
                entries.values.filter { it.dateEpochDay in startDate..endDate }
                    .sortedBy { it.dateEpochDay }
            }
    }
}
