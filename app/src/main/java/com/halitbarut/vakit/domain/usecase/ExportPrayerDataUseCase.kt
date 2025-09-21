package com.halitbarut.vakit.domain.usecase

import com.halitbarut.vakit.domain.model.PrayerType
import com.halitbarut.vakit.domain.repository.PrayerRepository
import javax.inject.Inject

class ExportPrayerDataUseCase @Inject constructor(
    private val prayerRepository: PrayerRepository,
) {

    suspend operator fun invoke(): String {
        val stats = prayerRepository.getPrayerStats()
            ?: throw IllegalStateException("Prayer stats are not initialized")
        val countsByType = stats.prayers.associateBy { it.type }
        val csvRows = buildList {
            add(HEADER_ROW)
            PrayerType.ordered.forEach { type ->
                val count = countsByType[type]
                val debt = count?.debt ?: 0
                val completed = count?.completed ?: 0
                val remaining = count?.remaining ?: (debt - completed).coerceAtLeast(0)
                add(
                    listOf(
                        type.displayName,
                        debt.toString(),
                        completed.toString(),
                        remaining.toString(),
                    ).joinToString(separator = ","),
                )
            }
        }
        return csvRows.joinToString(separator = NEW_LINE)
    }

    companion object {
        private const val HEADER_ROW = "Prayer,Initial_Debt,Completed,Remaining"
        private const val NEW_LINE = "\n"
    }
}
