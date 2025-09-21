package com.halitbarut.vakit.domain.usecase

import com.halitbarut.vakit.domain.model.PrayerType
import com.halitbarut.vakit.domain.repository.PrayerRepository
import javax.inject.Inject

class DecrementPrayerCompletionUseCase @Inject constructor(
    private val repository: PrayerRepository,
) {
    suspend operator fun invoke(prayerType: PrayerType, amount: Int = 1) {
        repository.decrementCompleted(prayerType, amount)
    }
}
