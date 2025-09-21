package com.halitbarut.vakit.domain.model

import androidx.annotation.DrawableRes
import com.halitbarut.vakit.R

enum class PrayerType(
    val displayName: String,
    @DrawableRes val iconRes: Int?,
) {
    FAJR(displayName = "Sabah", iconRes = null),
    DHUHR(displayName = "Öğle", iconRes = null),
    ASR(displayName = "İkindi", iconRes = null),
    MAGHRIB(displayName = "Akşam", iconRes = null),
    ISHA(displayName = "Yatsı", iconRes = null),
    WITR(displayName = "Vitir", iconRes = null);

    companion object {
        val ordered: List<PrayerType> = listOf(FAJR, DHUHR, ASR, MAGHRIB, ISHA, WITR)
    }
}
