package com.android.zenspend.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.budgetPlanStore: DataStore<Preferences> by preferencesDataStore(name = "budget_plan")

/** What the user last chose in the budget planner. Ids are category ids; nothing here is a default. */
data class StoredBudgetPlan(
    val incomeMinor: Long?,
    val needsPercent: Int?,
    val wantsPercent: Int?,
    val savingsPercent: Int?,
    val needsCategoryIds: Set<Long>,
    val wantsCategoryIds: Set<Long>
)

@Singleton
class BudgetPlanPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val income = longPreferencesKey("income_minor")
    private val needs = intPreferencesKey("needs_percent")
    private val wants = intPreferencesKey("wants_percent")
    private val savings = intPreferencesKey("savings_percent")
    private val needsIds = stringSetPreferencesKey("needs_category_ids")
    private val wantsIds = stringSetPreferencesKey("wants_category_ids")

    val plan: Flow<StoredBudgetPlan> = context.budgetPlanStore.data.map { p ->
        StoredBudgetPlan(
            incomeMinor = p[income],
            needsPercent = p[needs],
            wantsPercent = p[wants],
            savingsPercent = p[savings],
            needsCategoryIds = p[needsIds].toIds(),
            wantsCategoryIds = p[wantsIds].toIds()
        )
    }

    suspend fun save(plan: StoredBudgetPlan) {
        context.budgetPlanStore.edit { p ->
            plan.incomeMinor?.let { p[income] = it }
            plan.needsPercent?.let { p[needs] = it }
            plan.wantsPercent?.let { p[wants] = it }
            plan.savingsPercent?.let { p[savings] = it }
            p[needsIds] = plan.needsCategoryIds.map(Long::toString).toSet()
            p[wantsIds] = plan.wantsCategoryIds.map(Long::toString).toSet()
        }
    }

    private fun Set<String>?.toIds(): Set<Long> = this?.mapNotNull { it.toLongOrNull() }?.toSet().orEmpty()
}
