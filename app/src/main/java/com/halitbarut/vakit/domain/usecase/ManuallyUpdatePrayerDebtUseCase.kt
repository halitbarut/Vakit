package com.halitbarut.vakit.domain.usecase

import com.halitbarut.vakit.domain.model.PrayerType
import com.halitbarut.vakit.domain.repository.PrayerRepository
import javax.inject.Inject

class ManuallyUpdatePrayerDebtUseCase @Inject constructor(
    private val repository: PrayerRepository,
) {
    suspend operator fun invoke(prayerType: PrayerType, newDebt: Int) {
        repository.manuallyUpdateDebt(prayerType, newDebt)
    }
}
