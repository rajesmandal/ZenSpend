package com.android.zenspend.feature.budget.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.android.zenspend.core.ui.util.MoneyFormat
import androidx.lifecycle.viewModelScope
import com.android.zenspend.core.database.model.CategoryType
import com.android.zenspend.core.datastore.BudgetPlanPreferences
import com.android.zenspend.core.datastore.StoredBudgetPlan
import com.android.zenspend.feature.budget.domain.model.BudgetBucket
import com.android.zenspend.feature.budget.domain.model.BudgetPlanCalculator
import com.android.zenspend.feature.budget.domain.model.BudgetPlanResult
import com.android.zenspend.feature.budget.domain.model.BudgetSplit
import com.android.zenspend.feature.budget.domain.model.CategoryBucketClassifier
import com.android.zenspend.feature.budget.domain.usecase.ApplyBudgetPlanUseCase
import com.android.zenspend.feature.category.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

/** A spending category shown in the planner together with the bucket it currently belongs to. */
data class PlanCategory(val id: Long, val name: String, val bucket: BudgetBucket)

data class BudgetPlanUiState(
    val isLoading: Boolean = true,
    val isApplying: Boolean = false,
    val incomeText: String = "",
    val split: BudgetSplit = BudgetSplit.Balanced,
    val categories: List<PlanCategory> = emptyList(),
    val month: Calendar = Calendar.getInstance()
) {
    /** Income typed by the user in minor units (0 while empty or invalid). */
    val incomeMinor: Long
        get() = incomeText.toDoubleOrNull()?.let { Math.round(it * MINOR_PER_MAJOR) }?.coerceAtLeast(0L) ?: 0L

    val result: BudgetPlanResult
        get() = BudgetPlanCalculator.calculate(incomeMinor, split, categories.associate { it.id to it.bucket })

    /** Index of the matching preset chip, or [BudgetSplit.presets] size for "Custom". */
    val presetIndex: Int
        get() = BudgetSplit.presets.indexOf(split).takeIf { it >= 0 } ?: BudgetSplit.presets.size

    val canApply: Boolean get() = incomeMinor > 0 && split.isValid && !isApplying

    private companion object {
        const val MINOR_PER_MAJOR = 100
    }
}

sealed interface BudgetPlanEvent {
    data class OnIncomeChange(val text: String) : BudgetPlanEvent
    data class OnPresetSelected(val index: Int) : BudgetPlanEvent
    data class OnAdjust(val bucket: BudgetBucket, val delta: Int) : BudgetPlanEvent
    data class OnCategoryBucket(val categoryId: Long, val bucket: BudgetBucket) : BudgetPlanEvent
    object Apply : BudgetPlanEvent
}

sealed interface BudgetPlanEffect {
    data class Applied(val count: Int) : BudgetPlanEffect
    object Failed : BudgetPlanEffect
}

@HiltViewModel
class BudgetPlanViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val preferences: BudgetPlanPreferences,
    private val applyBudgetPlan: ApplyBudgetPlanUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetPlanUiState())
    val uiState: StateFlow<BudgetPlanUiState> = _uiState.asStateFlow()

    private val _effects = Channel<BudgetPlanEffect>()
    val effects: Flow<BudgetPlanEffect> = _effects.receiveAsFlow()

    init {
        viewModelScope.launch { initialise() }
    }

    fun handleEvent(event: BudgetPlanEvent) {
        when (event) {
            is BudgetPlanEvent.OnIncomeChange ->
                _uiState.update { it.copy(incomeText = event.text.filter { c -> c.isDigit() || c == '.' }) }
            is BudgetPlanEvent.OnPresetSelected ->
                BudgetSplit.presets.getOrNull(event.index)?.let { preset -> _uiState.update { it.copy(split = preset) } }
            is BudgetPlanEvent.OnAdjust ->
                _uiState.update { it.copy(split = it.split.adjust(event.bucket, event.delta)) }
            is BudgetPlanEvent.OnCategoryBucket -> _uiState.update { state ->
                state.copy(categories = state.categories.map {
                    if (it.id == event.categoryId) it.copy(bucket = event.bucket) else it
                })
            }
            BudgetPlanEvent.Apply -> apply()
        }
    }

    /** Loads categories and the last saved choices once; later edits are never overwritten. */
    private suspend fun initialise() {
        val stored = preferences.plan.first()
        val categories = categoryRepository.getAllActiveCategories().first()
            .filter { it.type != CategoryType.INCOME }
            .map { PlanCategory(it.id, it.name, bucketFor(it.id, it.name, stored)) }

        val savedSplit = stored.toSplit()
        _uiState.update {
            it.copy(
                isLoading = false,
                incomeText = stored.incomeMinor?.let(::formatIncome).orEmpty(),
                split = savedSplit ?: BudgetSplit.Balanced,
                categories = categories
            )
        }
    }

    private fun bucketFor(id: Long, name: String, stored: StoredBudgetPlan): BudgetBucket = when (id) {
        in stored.needsCategoryIds -> BudgetBucket.NEEDS
        in stored.wantsCategoryIds -> BudgetBucket.WANTS
        else -> CategoryBucketClassifier.classify(name)
    }

    private fun StoredBudgetPlan.toSplit(): BudgetSplit? {
        val split = BudgetSplit(needsPercent ?: return null, wantsPercent ?: return null, savingsPercent ?: return null)
        return split.takeIf { it.isValid }
    }

    private fun formatIncome(minor: Long): String =
        MoneyFormat.rawFromMinor(minor)

    private fun apply() {
        val state = _uiState.value
        if (!state.canApply) return
        viewModelScope.launch {
            _uiState.update { it.copy(isApplying = true) }
            try {
                preferences.save(
                    StoredBudgetPlan(
                        incomeMinor = state.incomeMinor,
                        needsPercent = state.split.needsPercent,
                        wantsPercent = state.split.wantsPercent,
                        savingsPercent = state.split.savingsPercent,
                        needsCategoryIds = state.categories.idsIn(BudgetBucket.NEEDS),
                        wantsCategoryIds = state.categories.idsIn(BudgetBucket.WANTS)
                    )
                )
                val (start, end) = monthRange(state.month)
                val count = applyBudgetPlan(state.result, start, end)
                _effects.send(BudgetPlanEffect.Applied(count))
            } catch (e: Exception) {
                _effects.send(BudgetPlanEffect.Failed)
            } finally {
                _uiState.update { it.copy(isApplying = false) }
            }
        }
    }

    private fun List<PlanCategory>.idsIn(bucket: BudgetBucket): Set<Long> =
        filter { it.bucket == bucket }.map { it.id }.toSet()

    private fun monthRange(calendar: Calendar): Pair<Long, Long> {
        val start = (calendar.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val end = (start.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }
        return start.timeInMillis to end.timeInMillis
    }

    private companion object {
        const val MINOR_PER_MAJOR = 100
    }
}
