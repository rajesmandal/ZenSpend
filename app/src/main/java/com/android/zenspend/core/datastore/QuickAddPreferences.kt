package com.android.zenspend.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.quickAddStore: DataStore<Preferences> by preferencesDataStore(name = "quick_add")

/** Settings for the always-there "add Income / Spent" notification. Off until the user turns it on. */
@Singleton
class QuickAddPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationEnabled = booleanPreferencesKey("notification_enabled")

    val isNotificationEnabled: Flow<Boolean> =
        context.quickAddStore.data.map { it[notificationEnabled] ?: false }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.quickAddStore.edit { it[notificationEnabled] = enabled }
    }
}
