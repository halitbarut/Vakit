package com.halitbarut.vakit.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

data class NotificationPreferences(
    val isEnabled: Boolean,
    val time: String,
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val preferencesFlow: Flow<NotificationPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            NotificationPreferences(
                isEnabled = preferences[NOTIFICATIONS_ENABLED_KEY] ?: false,
                time = preferences[NOTIFICATION_TIME_KEY] ?: DEFAULT_NOTIFICATION_TIME,
            )
        }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }

    suspend fun setNotificationTime(time: String) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_TIME_KEY] = time
        }
    }

    companion object {
        const val DEFAULT_NOTIFICATION_TIME = "20:30"

        private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
        private val NOTIFICATION_TIME_KEY = stringPreferencesKey("notification_time")
    }
}
