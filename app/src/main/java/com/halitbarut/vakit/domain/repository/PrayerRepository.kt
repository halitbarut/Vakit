package com.halitbarut.vakit.domain.repository

import com.halitbarut.vakit.domain.model.DailyPrayerLog
import com.halitbarut.vakit.domain.model.PrayerStats
import com.halitbarut.vakit.domain.model.PrayerType
import kotlinx.coroutines.flow.Flow

interface PrayerRepository {
    fun observePrayerStats(): Flow<PrayerStats?>
    fun observeDailyLogs(startDateEpochDay: Long, endDateEpochDay: Long): Flow<List<DailyPrayerLog>>
    suspend fun getPrayerStats(): PrayerStats?
    suspend fun initializePrayerStats(
        debts: Map<PrayerType, Int>,
        isWitrTracked: Boolean,
        nowMillis: Long,
    )
    suspend fun incrementDebt(prayerType: PrayerType)
    suspend fun incrementCompleted(prayerType: PrayerType, amount: Int = 1)
    suspend fun decrementCompleted(prayerType: PrayerType, amount: Int = 1)
    suspend fun manuallyUpdateDebt(prayerType: PrayerType, newDebt: Int)
    suspend fun toggleWitrTracking(enabled: Boolean)
    suspend fun resetAll()
    suspend fun refreshDailyProgress(nowMillis: Long)
}
