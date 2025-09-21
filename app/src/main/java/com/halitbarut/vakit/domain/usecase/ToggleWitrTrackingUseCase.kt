package com.halitbarut.vakit.domain.usecase

import com.halitbarut.vakit.domain.repository.PrayerRepository
import javax.inject.Inject

class ToggleWitrTrackingUseCase @Inject constructor(
    private val repository: PrayerRepository,
) {
    suspend operator fun invoke(enabled: Boolean) {
        repository.toggleWitrTracking(enabled)
    }
}
