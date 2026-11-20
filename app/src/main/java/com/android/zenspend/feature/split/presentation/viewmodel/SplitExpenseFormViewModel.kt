package com.android.zenspend.feature.split.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.zenspend.R
import com.android.zenspend.core.ui.util.MoneyFormat
import com.android.zenspend.feature.split.domain.model.SplitExpense
import com.android.zenspend.feature.split.domain.model.SplitMath
import com.android.zenspend.feature.split.domain.model.SplitType
import com.android.zenspend.feature.split.domain.repository.SplitRepository
import com.android.zenspend.feature.split.presentation.contract.SplitExpenseFormUiEffect
import com.android.zenspend.feature.split.presentation.contract.SplitExpenseFormUiEvent
import com.android.zenspend.feature.split.presentation.contract.SplitExpenseFormUiState
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SplitExpenseFormViewModel @Inject constructor(
    private val repository: SplitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplitExpenseFormUiState())
    val uiState: StateFlow<SplitExpenseFormUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<SplitExpenseFormUiEffect>()
    val uiEffect: Flow<SplitExpenseFormUiEffect> = _uiEffect.receiveAsFlow()

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private var initialised = false
    private var groupId = 0L

    fun handleEvent(event: SplitExpenseFormUiEvent) {
        when (event) {
            is SplitExpenseFormUiEvent.Init -> init(event.groupId, event.expenseId)
            is SplitExpenseFormUiEvent.Title -> edit { it.copy(title = event.v) }
            is SplitExpenseFormUiEvent.Amount -> edit { it.copy(amountText = event.v) }
            is SplitExpenseFormUiEvent.PaidBy -> edit { it.copy(paidBy = event.memberId) }
            is SplitExpenseFormUiEvent.Type -> edit { it.copy(splitType = event.type) }
            is SplitExpenseFormUiEvent.Toggle -> edit {
                it.copy(selected = if (event.memberId in it.selected) it.selected - event.memberId else it.selected + event.memberId)
            }
            SplitExpenseFormUiEvent.SelectAll -> edit { it.copy(selected = it.members.map { m -> m.id }.toSet()) }
            is SplitExpenseFormUiEvent.Exact -> edit { it.copy(exactTexts = it.exactTexts + (event.memberId to event.v)) }
            is SplitExpenseFormUiEvent.Date -> edit {
                it.copy(dateMillis = event.millis, dateLabel = dateFormat.format(Date(event.millis)))
            }
            is SplitExpenseFormUiEvent.Note -> edit { it.copy(note = event.v) }
            SplitExpenseFormUiEvent.Save -> save()
        }
    }

    /** Any change clears the previous error. */
    private fun edit(block: (SplitExpenseFormUiState) -> SplitExpenseFormUiState) =
        _uiState.update { block(it).copy(errorRes = null, errorArg = null) }

    private fun init(groupId: Long, expenseId: Long) {
        if (initialised) return
        initialised = true
        viewModelScope.launch {
            val existing = if (expenseId > 0) repository.getExpense(expenseId) else null
            this@SplitExpenseFormViewModel.groupId = existing?.groupId ?: groupId
            val details = repository.observeGroup(this@SplitExpenseFormViewModel.groupId).first() ?: return@launch
            val everyone = details.members.map { it.id }.toSet()
            val now = System.currentTimeMillis()
            _uiState.update {
                if (existing == null) {
                    it.copy(
                        groupName = details.group.name, members = details.members,
                        paidBy = details.me?.id ?: details.members.firstOrNull()?.id ?: 0L,
                        selected = everyone, dateMillis = now, dateLabel = dateFormat.format(Date(now))
                    )
                } else {
                    it.copy(
                        expenseId = existing.id, groupName = details.group.name, members = details.members,
                        title = existing.title, amountText = MoneyFormat.rawFromMinor(existing.amount),
                        paidBy = existing.paidBy, splitType = existing.splitType,
                        selected = existing.shares.keys,
                        exactTexts = existing.shares.mapValues { (_, v) -> MoneyFormat.rawFromMinor(v) },
                        dateMillis = existing.date, dateLabel = dateFormat.format(Date(existing.date)),
                        note = existing.note.orEmpty()
                    )
                }
            }
        }
    }

    private fun save() {
        val s = _uiState.value
        val amount = parseMinor(s.amountText) ?: return fail(R.string.split_error_amount)
        if (s.paidBy == 0L) return fail(R.string.split_error_payer)

        val shares: Map<Long, Long> = when (s.splitType) {
            SplitType.EQUAL -> {
                // Keep the on-screen member order so leftover paise land on the same people the preview showed.
                val chosen = s.members.map { it.id }.filter { it in s.selected }
                if (chosen.isEmpty()) return fail(R.string.split_error_nobody)
                SplitMath.equalShares(amount, chosen)
            }
            SplitType.EXACT -> {
                val typed = s.members.mapNotNull { m ->
                    val text = s.exactTexts[m.id].orEmpty()
                    if (text.isBlank()) null else (parseMinor(text) ?: return fail(R.string.split_error_exact_invalid)).let { m.id to it }
                }.toMap()
                if (typed.isEmpty()) return fail(R.string.split_error_nobody)
                if (!SplitMath.exactMatchesTotal(amount, typed)) {
                    val diff = amount - typed.values.sum()
                    return _uiState.update {
                        it.copy(errorRes = R.string.split_error_exact_sum, errorArg = MoneyFormat.format(kotlin.math.abs(diff)))
                    }
                }
                typed
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorRes = null) }
            val id = repository.saveExpense(
                SplitExpense(
                    id = s.expenseId ?: 0L, groupId = groupId, title = s.title, amount = amount,
                    paidBy = s.paidBy, date = s.dateMillis, shares = shares, splitType = s.splitType,
                    note = s.note.trim().ifBlank { null }
                )
            )
            if (id == null) {
                _uiState.update { it.copy(isSubmitting = false, errorRes = R.string.split_error_save) }
            } else {
                _uiEffect.send(SplitExpenseFormUiEffect.NavigateBack)
            }
        }
    }

    private fun fail(res: Int) = _uiState.update { it.copy(errorRes = res, errorArg = null) }

    private fun parseMinor(text: String): Long? {
        val value = text.trim().toDoubleOrNull() ?: return null
        return if (value > 0 && value <= MAX_MAJOR) Math.round(value * MINOR_PER_MAJOR) else null
    }

    private companion object {
        const val MINOR_PER_MAJOR = 100
        const val MAX_MAJOR = 10_000_000_000.0
    }
}
