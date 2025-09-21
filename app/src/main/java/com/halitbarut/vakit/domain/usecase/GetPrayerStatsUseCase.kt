package com.halitbarut.vakit.domain.usecase

import com.halitbarut.vakit.domain.repository.PrayerRepository
import javax.inject.Inject

class GetPrayerStatsUseCase @Inject constructor(
    private val repository: PrayerRepository,
) {
    suspend operator fun invoke() = repository.getPrayerStats()
}
