package com.halitbarut.vakit.domain.usecase

import com.halitbarut.vakit.domain.model.PrayerType
import com.halitbarut.vakit.domain.repository.PrayerRepository
import javax.inject.Inject

class InitializePrayerStatsUseCase @Inject constructor(
    private val repository: PrayerRepository,
) {
    suspend operator fun invoke(
        debts: Map<PrayerType, Int>,
        isWitrTracked: Boolean,
        nowMillis: Long,
    ) {
        repository.initializePrayerStats(debts, isWitrTracked, nowMillis)
    }
}
