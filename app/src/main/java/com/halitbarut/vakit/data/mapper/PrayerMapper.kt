package com.halitbarut.vakit.data.mapper

import com.halitbarut.vakit.data.local.entity.PrayerEntity
import com.halitbarut.vakit.domain.model.PrayerCount
import com.halitbarut.vakit.domain.model.PrayerStats
import com.halitbarut.vakit.domain.model.PrayerType

fun PrayerEntity.toDomain(): PrayerStats = PrayerStats(
    startDateMillis = startDateMillis,
    lastDailyResetEpochMillis = lastDailyResetEpochMillis,
    prayers = buildList {
        add(
            PrayerCount(
                type = PrayerType.FAJR,
                debt = fajrDebt,
                completed = fajrCompleted,
                completedToday = fajrCompletedToday,
                lastUpdateMillis = fajrLastUpdateMillis,
            ),
        )
        add(
            PrayerCount(
                type = PrayerType.DHUHR,
                debt = dhuhrDebt,
                completed = dhuhrCompleted,
                completedToday = dhuhrCompletedToday,
                lastUpdateMillis = dhuhrLastUpdateMillis,
            ),
        )
        add(
            PrayerCount(
                type = PrayerType.ASR,
                debt = asrDebt,
                completed = asrCompleted,
                completedToday = asrCompletedToday,
                lastUpdateMillis = asrLastUpdateMillis,
            ),
        )
        add(
            PrayerCount(
                type = PrayerType.MAGHRIB,
                debt = maghribDebt,
                completed = maghribCompleted,
                completedToday = maghribCompletedToday,
                lastUpdateMillis = maghribLastUpdateMillis,
            ),
        )
        add(
            PrayerCount(
                type = PrayerType.ISHA,
                debt = ishaDebt,
                completed = ishaCompleted,
                completedToday = ishaCompletedToday,
                lastUpdateMillis = ishaLastUpdateMillis,
            ),
        )
        add(
            PrayerCount(
                type = PrayerType.WITR,
                debt = witrDebt,
                completed = witrCompleted,
                completedToday = witrCompletedToday,
                lastUpdateMillis = witrLastUpdateMillis,
            ),
        )
    },
    isWitrTracked = isWitrTracked,
    bestDailyCompletion = bestDailyCompletion,
)
