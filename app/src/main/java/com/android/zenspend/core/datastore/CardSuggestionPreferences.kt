package com.android.zenspend.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.cardSuggestionStore: DataStore<Preferences> by preferencesDataStore(name = "card_suggestions")

/** Cards the user said "Not now" to, so the same suggestion does not keep coming back. */
@Singleton
class CardSuggestionPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dismissedKey = stringSetPreferencesKey("dismissed_last4")

    val dismissed: Flow<Set<String>> = context.cardSuggestionStore.data.map { it[dismissedKey].orEmpty() }

    suspend fun dismiss(last4: String) {
        context.cardSuggestionStore.edit { it[dismissedKey] = it[dismissedKey].orEmpty() + last4 }
    }
}
