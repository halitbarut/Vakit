package com.halitbarut.vakit.data.repository

import com.halitbarut.vakit.data.local.dao.DailyLogDao
import com.halitbarut.vakit.data.local.dao.PrayerDao
import com.halitbarut.vakit.data.local.entity.DailyLogEntity
import com.halitbarut.vakit.data.local.entity.PrayerEntity
import com.halitbarut.vakit.data.mapper.toDomain
import com.halitbarut.vakit.di.IoDispatcher
import com.halitbarut.vakit.domain.model.DailyPrayerLog
import com.halitbarut.vakit.domain.model.PrayerStats
import com.halitbarut.vakit.domain.model.PrayerType
import com.halitbarut.vakit.domain.repository.PrayerRepository
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

class PrayerRepositoryImpl @Inject constructor(
    private val prayerDao: PrayerDao,
    private val dailyLogDao: DailyLogDao,
    private val clock: Clock,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : PrayerRepository {

    override fun observePrayerStats(): Flow<PrayerStats?> =
        prayerDao.observePrayerStats()
            .onEach { entity -> entity?.let { refreshDailyProgressIfNeeded(it, clock.millis()) } }
            .map { entity -> entity?.toDomain() }

    override fun observeDailyLogs(
        startDateEpochDay: Long,
        endDateEpochDay: Long,
    ): Flow<List<DailyPrayerLog>> =
        dailyLogDao.getLogsForDateRange(startDateEpochDay, endDateEpochDay)
            .map { logs -> logs.map { it.toDomain() } }

    override suspend fun getPrayerStats(): PrayerStats? = withContext(ioDispatcher) {
        prayerDao.getPrayerStats()?.let { entity ->
            refreshDailyProgressIfNeeded(entity, clock.millis())
            prayerDao.getPrayerStats()?.toDomain()
        }
    }

    override suspend fun initializePrayerStats(
        debts: Map<PrayerType, Int>,
        isWitrTracked: Boolean,
        nowMillis: Long,
    ) = withContext(ioDispatcher) {
        val sanitizedDebts = sanitizeDebts(debts, isWitrTracked)
        val baseline = PrayerEntity(
            startDateMillis = nowMillis,
            fajrDebt = sanitizedDebts[PrayerType.FAJR] ?: 0,
            fajrCompleted = 0,
            fajrCompletedToday = 0,
            fajrLastUpdateMillis = nowMillis,
            dhuhrDebt = sanitizedDebts[PrayerType.DHUHR] ?: 0,
            dhuhrCompleted = 0,
            dhuhrCompletedToday = 0,
            dhuhrLastUpdateMillis = nowMillis,
            asrDebt = sanitizedDebts[PrayerType.ASR] ?: 0,
            asrCompleted = 0,
            asrCompletedToday = 0,
            asrLastUpdateMillis = nowMillis,
            maghribDebt = sanitizedDebts[PrayerType.MAGHRIB] ?: 0,
            maghribCompleted = 0,
            maghribCompletedToday = 0,
            maghribLastUpdateMillis = nowMillis,
            ishaDebt = sanitizedDebts[PrayerType.ISHA] ?: 0,
            ishaCompleted = 0,
            ishaCompletedToday = 0,
            ishaLastUpdateMillis = nowMillis,
            witrDebt = sanitizedDebts[PrayerType.WITR] ?: 0,
            witrCompleted = 0,
            witrCompletedToday = 0,
            witrLastUpdateMillis = nowMillis,
            isWitrTracked = isWitrTracked,
            lastDailyResetEpochMillis = nowMillis,
            bestDailyCompletion = 0,
        )
        prayerDao.upsert(baseline)
    }

    override suspend fun incrementDebt(prayerType: PrayerType) = withContext(ioDispatcher) {
        val current = prayerDao.getPrayerStats() ?: return@withContext
        if (!current.isWitrTracked && prayerType == PrayerType.WITR) return@withContext
        val updated = current.incrementDebtValue(prayerType, clock.millis())
        prayerDao.upsert(updated)
    }

    override suspend fun incrementCompleted(prayerType: PrayerType, amount: Int) = withContext(ioDispatcher) {
        val current = prayerDao.getPrayerStats() ?: return@withContext
        if (!current.isWitrTracked && prayerType == PrayerType.WITR) return@withContext
        val safeAmount = amount.coerceAtLeast(0)
        if (safeAmount == 0) return@withContext
        val timestamp = clock.millis()
        val updated = current.incrementPrayer(prayerType, safeAmount, timestamp)
        prayerDao.upsert(updated)
        adjustDailyLog(prayerType, safeAmount, timestamp)
    }

    override suspend fun decrementCompleted(prayerType: PrayerType, amount: Int) = withContext(ioDispatcher) {
        val current = prayerDao.getPrayerStats() ?: return@withContext
        if (!current.isWitrTracked && prayerType == PrayerType.WITR) return@withContext
        val safeAmount = amount.coerceAtLeast(0)
        if (safeAmount == 0) return@withContext
        val currentCompleted = current.completedCount(prayerType)
        if (currentCompleted == 0) return@withContext
        val actualDelta = safeAmount.coerceAtMost(currentCompleted)
        val timestamp = clock.millis()
        val updated = current.decrementPrayer(prayerType, actualDelta, timestamp)
        prayerDao.upsert(updated)
        adjustDailyLog(prayerType, -actualDelta, timestamp)
    }

    override suspend fun manuallyUpdateDebt(prayerType: PrayerType, newDebt: Int) = withContext(ioDispatcher) {
        val current = prayerDao.getPrayerStats() ?: return@withContext
        if (!current.isWitrTracked && prayerType == PrayerType.WITR) return@withContext
        val sanitizedDebt = newDebt.coerceAtLeast(0)
        val completedCount = current.completedCount(prayerType)
        val totalDebt = (completedCount.toLong() + sanitizedDebt.toLong())
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()
        val updated = current.updateTotalDebt(prayerType, totalDebt, clock.millis())
        prayerDao.upsert(updated)
    }

    override suspend fun toggleWitrTracking(enabled: Boolean) = withContext(ioDispatcher) {
        val current = prayerDao.getPrayerStats() ?: return@withContext
        val updated = if (enabled) {
            current.copy(
                isWitrTracked = true,
                witrLastUpdateMillis = clock.millis(),
            )
        } else {
            current.copy(
                isWitrTracked = false,
                witrDebt = 0,
                witrCompleted = 0,
                witrCompletedToday = 0,
                witrLastUpdateMillis = clock.millis(),
            )
        }
        prayerDao.upsert(updated)
    }

    override suspend fun resetAll() = withContext(ioDispatcher) {
        prayerDao.clearAll()
        dailyLogDao.clearAll()
    }

    override suspend fun refreshDailyProgress(nowMillis: Long) = withContext(ioDispatcher) {
        val entity = prayerDao.getPrayerStats() ?: return@withContext
        refreshDailyProgressIfNeeded(entity, nowMillis)
    }

    private suspend fun refreshDailyProgressIfNeeded(entity: PrayerEntity, nowMillis: Long) {
        val needsReset = !isSameDay(entity.lastDailyResetEpochMillis, nowMillis)
        if (!needsReset) return
        val totalCompletedToday = entity.totalCompletedToday()
        val updatedBest = maxOf(entity.bestDailyCompletion, totalCompletedToday)
        val reset = entity.copy(
            fajrCompletedToday = 0,
            dhuhrCompletedToday = 0,
            asrCompletedToday = 0,
            maghribCompletedToday = 0,
            ishaCompletedToday = 0,
            witrCompletedToday = 0,
            lastDailyResetEpochMillis = nowMillis,
            bestDailyCompletion = updatedBest,
        )
        prayerDao.upsert(reset)
    }

    private fun sanitizeDebts(debts: Map<PrayerType, Int>, isWitrTracked: Boolean): Map<PrayerType, Int> {
        val sanitized = debts.mapValues { (_, value) -> value.coerceAtLeast(0) }.toMutableMap()
        if (!isWitrTracked) {
            sanitized[PrayerType.WITR] = 0
        }
        return sanitized
    }

    private fun PrayerEntity.incrementPrayer(prayerType: PrayerType, amount: Int, timestamp: Long): PrayerEntity {
        val safeAmount = amount.coerceAtLeast(0)
        if (safeAmount == 0) return this
        return when (prayerType) {
            PrayerType.FAJR -> copy(
                fajrCompleted = fajrCompleted + safeAmount,
                fajrCompletedToday = fajrCompletedToday + safeAmount,
                fajrLastUpdateMillis = timestamp,
            )
            PrayerType.DHUHR -> copy(
                dhuhrCompleted = dhuhrCompleted + safeAmount,
                dhuhrCompletedToday = dhuhrCompletedToday + safeAmount,
                dhuhrLastUpdateMillis = timestamp,
            )
            PrayerType.ASR -> copy(
                asrCompleted = asrCompleted + safeAmount,
                asrCompletedToday = asrCompletedToday + safeAmount,
                asrLastUpdateMillis = timestamp,
            )
            PrayerType.MAGHRIB -> copy(
                maghribCompleted = maghribCompleted + safeAmount,
                maghribCompletedToday = maghribCompletedToday + safeAmount,
                maghribLastUpdateMillis = timestamp,
            )
            PrayerType.ISHA -> copy(
                ishaCompleted = ishaCompleted + safeAmount,
                ishaCompletedToday = ishaCompletedToday + safeAmount,
                ishaLastUpdateMillis = timestamp,
            )
            PrayerType.WITR -> copy(
                witrCompleted = witrCompleted + safeAmount,
                witrCompletedToday = witrCompletedToday + safeAmount,
                witrLastUpdateMillis = timestamp,
            )
        }
    }

    private fun PrayerEntity.decrementPrayer(prayerType: PrayerType, amount: Int, timestamp: Long): PrayerEntity {
        val safeAmount = amount.coerceAtLeast(0)
        if (safeAmount == 0) return this
        return when (prayerType) {
            PrayerType.FAJR -> copy(
                fajrCompleted = (fajrCompleted - safeAmount).coerceAtLeast(0),
                fajrCompletedToday = (fajrCompletedToday - safeAmount).coerceAtLeast(0),
                fajrLastUpdateMillis = timestamp,
            )
            PrayerType.DHUHR -> copy(
                dhuhrCompleted = (dhuhrCompleted - safeAmount).coerceAtLeast(0),
                dhuhrCompletedToday = (dhuhrCompletedToday - safeAmount).coerceAtLeast(0),
                dhuhrLastUpdateMillis = timestamp,
            )
            PrayerType.ASR -> copy(
                asrCompleted = (asrCompleted - safeAmount).coerceAtLeast(0),
                asrCompletedToday = (asrCompletedToday - safeAmount).coerceAtLeast(0),
                asrLastUpdateMillis = timestamp,
            )
            PrayerType.MAGHRIB -> copy(
                maghribCompleted = (maghribCompleted - safeAmount).coerceAtLeast(0),
                maghribCompletedToday = (maghribCompletedToday - safeAmount).coerceAtLeast(0),
                maghribLastUpdateMillis = timestamp,
            )
            PrayerType.ISHA -> copy(
                ishaCompleted = (ishaCompleted - safeAmount).coerceAtLeast(0),
                ishaCompletedToday = (ishaCompletedToday - safeAmount).coerceAtLeast(0),
                ishaLastUpdateMillis = timestamp,
            )
            PrayerType.WITR -> copy(
                witrCompleted = (witrCompleted - safeAmount).coerceAtLeast(0),
                witrCompletedToday = (witrCompletedToday - safeAmount).coerceAtLeast(0),
                witrLastUpdateMillis = timestamp,
            )
        }
    }

    private fun PrayerEntity.incrementDebtValue(prayerType: PrayerType, timestamp: Long): PrayerEntity = when (prayerType) {
        PrayerType.FAJR -> copy(
            fajrDebt = fajrDebt + 1,
            fajrLastUpdateMillis = timestamp,
        )
        PrayerType.DHUHR -> copy(
            dhuhrDebt = dhuhrDebt + 1,
            dhuhrLastUpdateMillis = timestamp,
        )
        PrayerType.ASR -> copy(
            asrDebt = asrDebt + 1,
            asrLastUpdateMillis = timestamp,
        )
        PrayerType.MAGHRIB -> copy(
            maghribDebt = maghribDebt + 1,
            maghribLastUpdateMillis = timestamp,
        )
        PrayerType.ISHA -> copy(
            ishaDebt = ishaDebt + 1,
            ishaLastUpdateMillis = timestamp,
        )
        PrayerType.WITR -> copy(
            witrDebt = witrDebt + 1,
            witrLastUpdateMillis = timestamp,
        )
    }

    private fun PrayerEntity.updateTotalDebt(prayerType: PrayerType, debtTotal: Int, timestamp: Long): PrayerEntity =
        when (prayerType) {
            PrayerType.FAJR -> copy(
                fajrDebt = debtTotal,
                fajrLastUpdateMillis = timestamp,
            )
            PrayerType.DHUHR -> copy(
                dhuhrDebt = debtTotal,
                dhuhrLastUpdateMillis = timestamp,
            )
            PrayerType.ASR -> copy(
                asrDebt = debtTotal,
                asrLastUpdateMillis = timestamp,
            )
            PrayerType.MAGHRIB -> copy(
                maghribDebt = debtTotal,
                maghribLastUpdateMillis = timestamp,
            )
            PrayerType.ISHA -> copy(
                ishaDebt = debtTotal,
                ishaLastUpdateMillis = timestamp,
            )
            PrayerType.WITR -> copy(
                witrDebt = debtTotal,
                witrLastUpdateMillis = timestamp,
            )
        }

    private fun PrayerEntity.completedCount(prayerType: PrayerType): Int = when (prayerType) {
        PrayerType.FAJR -> fajrCompleted
        PrayerType.DHUHR -> dhuhrCompleted
        PrayerType.ASR -> asrCompleted
        PrayerType.MAGHRIB -> maghribCompleted
        PrayerType.ISHA -> ishaCompleted
        PrayerType.WITR -> witrCompleted
    }

    private suspend fun adjustDailyLog(prayerType: PrayerType, delta: Int, timestamp: Long) {
        if (delta == 0) return
        val epochDay = currentEpochDay(timestamp)
        val existing = dailyLogDao.getByDate(epochDay)
        val baseline = existing ?: DailyLogEntity(
            dateEpochDay = epochDay,
            fajrCompleted = 0,
            dhuhrCompleted = 0,
            asrCompleted = 0,
            maghribCompleted = 0,
            ishaCompleted = 0,
            witrCompleted = 0,
        )
        val updated = baseline.adjust(prayerType, delta)
        dailyLogDao.upsert(updated)
    }

    private fun DailyLogEntity.adjust(prayerType: PrayerType, delta: Int): DailyLogEntity {
        val applyDelta: (Int) -> Int = { value -> (value + delta).coerceAtLeast(0) }
        return when (prayerType) {
            PrayerType.FAJR -> copy(fajrCompleted = applyDelta(fajrCompleted))
            PrayerType.DHUHR -> copy(dhuhrCompleted = applyDelta(dhuhrCompleted))
            PrayerType.ASR -> copy(asrCompleted = applyDelta(asrCompleted))
            PrayerType.MAGHRIB -> copy(maghribCompleted = applyDelta(maghribCompleted))
            PrayerType.ISHA -> copy(ishaCompleted = applyDelta(ishaCompleted))
            PrayerType.WITR -> copy(witrCompleted = applyDelta(witrCompleted))
        }
    }

    private fun DailyLogEntity.toDomain(): DailyPrayerLog = DailyPrayerLog(
        dateEpochDay = dateEpochDay,
        fajrCompleted = fajrCompleted,
        dhuhrCompleted = dhuhrCompleted,
        asrCompleted = asrCompleted,
        maghribCompleted = maghribCompleted,
        ishaCompleted = ishaCompleted,
        witrCompleted = witrCompleted,
    )

    private fun currentEpochDay(timestamp: Long): Long =
        Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()

    private fun isSameDay(previousMillis: Long, nowMillis: Long): Boolean {
        val zoneId = ZoneId.systemDefault()
        val previousDate = Instant.ofEpochMilli(previousMillis).atZone(zoneId).toLocalDate()
        val nowDate = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
        return previousDate.isEqual(nowDate)
    }

    private fun Clock.millis(): Long = instant().toEpochMilli()
}
