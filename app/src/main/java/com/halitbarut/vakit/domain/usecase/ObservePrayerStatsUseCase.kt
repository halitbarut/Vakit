package com.halitbarut.vakit.domain.usecase

import com.halitbarut.vakit.domain.repository.PrayerRepository
import javax.inject.Inject

class ObservePrayerStatsUseCase @Inject constructor(
    private val repository: PrayerRepository,
) {
    operator fun invoke() = repository.observePrayerStats()
}
