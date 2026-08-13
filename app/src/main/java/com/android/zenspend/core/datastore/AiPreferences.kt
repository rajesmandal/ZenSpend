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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ai_preferences")

@Singleton
class AiPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val DISCLAIMER_ACCEPTED = booleanPreferencesKey("disclaimer_accepted")

    val isDisclaimerAccepted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DISCLAIMER_ACCEPTED] ?: false
        }

    suspend fun setDisclaimerAccepted(accepted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DISCLAIMER_ACCEPTED] = accepted
        }
    }
}
