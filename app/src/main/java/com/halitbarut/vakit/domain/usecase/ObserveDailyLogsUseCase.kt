package com.halitbarut.vakit.domain.usecase

import com.halitbarut.vakit.domain.model.DailyPrayerLog
import com.halitbarut.vakit.domain.repository.PrayerRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveDailyLogsUseCase @Inject constructor(
    private val repository: PrayerRepository,
) {
    operator fun invoke(startDateEpochDay: Long, endDateEpochDay: Long): Flow<List<DailyPrayerLog>> =
        repository.observeDailyLogs(startDateEpochDay, endDateEpochDay)
}
