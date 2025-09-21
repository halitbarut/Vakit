package com.halitbarut.vakit.domain.usecase

import com.halitbarut.vakit.domain.repository.PrayerRepository
import javax.inject.Inject

class RefreshDailyProgressUseCase @Inject constructor(
    private val repository: PrayerRepository,
) {
    suspend operator fun invoke(nowMillis: Long) {
        repository.refreshDailyProgress(nowMillis)
    }
}
