package com.android.zenspend.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ai_preferences")

@Singleton
class AiPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val DISCLAIMER_ACCEPTED = booleanPreferencesKey("disclaimer_accepted")
    private val SMS_DETECTION_ENABLED = booleanPreferencesKey("sms_detection_enabled")
    private val DAILY_USAGE_COUNT = intPreferencesKey("daily_usage_count")
    private val LAST_USAGE_DATE = longPreferencesKey("last_usage_date")

    val isDisclaimerAccepted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DISCLAIMER_ACCEPTED] ?: false
        }

    val isSmsDetectionEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[SMS_DETECTION_ENABLED] ?: false
        }

    val dailyUsage: Flow<Int> = context.dataStore.data
        .map { preferences ->
            val lastDate = preferences[LAST_USAGE_DATE] ?: 0L
            if (isToday(lastDate)) {
                preferences[DAILY_USAGE_COUNT] ?: 0
            } else {
                0
            }
        }

    suspend fun setDisclaimerAccepted(accepted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DISCLAIMER_ACCEPTED] = accepted
        }
    }

    suspend fun setSmsDetectionEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SMS_DETECTION_ENABLED] = enabled
        }
    }

    suspend fun incrementUsage() {
        context.dataStore.edit { preferences ->
            val lastDate = preferences[LAST_USAGE_DATE] ?: 0L
            val currentCount = if (isToday(lastDate)) {
                preferences[DAILY_USAGE_COUNT] ?: 0
            } else {
                0
            }
            preferences[DAILY_USAGE_COUNT] = currentCount + 1
            preferences[LAST_USAGE_DATE] = System.currentTimeMillis()
        }
    }

    private fun isToday(timestamp: Long): Boolean {
        val cal1 = java.util.Calendar.getInstance()
        val cal2 = java.util.Calendar.getInstance()
        cal1.timeInMillis = timestamp
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }
}
