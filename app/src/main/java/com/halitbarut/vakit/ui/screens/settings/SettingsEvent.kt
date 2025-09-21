package com.halitbarut.vakit.ui.screens.settings

sealed interface SettingsEvent {
    data class ShareCsv(val content: String) : SettingsEvent
    object ExportFailed : SettingsEvent
}
