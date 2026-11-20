package com.android.zenspend.feature.split.presentation.contract

import com.android.zenspend.feature.split.domain.model.SplitGroupDetails
import com.android.zenspend.feature.split.domain.model.SplitGroupOverview
import com.android.zenspend.feature.split.domain.model.SplitMember
import com.android.zenspend.feature.split.domain.model.SplitType

// ---- Groups list ----
data class SplitGroupsUiState(
    val isLoading: Boolean = true,
    val groups: List<SplitGroupOverview> = emptyList()
)

sealed interface SplitGroupsUiEvent {
    data class CreateGroup(val name: String, val myName: String, val others: List<String>) : SplitGroupsUiEvent
}

// ---- Group details ----
data class SplitGroupUiState(
    val isLoading: Boolean = true,
    val details: SplitGroupDetails? = null
)

sealed interface SplitGroupUiEvent {
    data class AddMember(val name: String) : SplitGroupUiEvent
    data class RemoveMember(val memberId: Long) : SplitGroupUiEvent
    data class SettleUp(val from: Long, val to: Long, val amount: Long) : SplitGroupUiEvent
    data class DeleteSettlement(val id: Long) : SplitGroupUiEvent
    data class DeleteExpense(val id: Long) : SplitGroupUiEvent
    object DeleteGroup : SplitGroupUiEvent
}

sealed interface SplitGroupUiEffect {
    data class ShowMessage(val messageRes: Int) : SplitGroupUiEffect
    object NavigateBack : SplitGroupUiEffect
}

// ---- Expense form ----
data class SplitExpenseFormUiState(
    val expenseId: Long? = null,
    val groupName: String = "",
    val members: List<SplitMember> = emptyList(),
    val title: String = "",
    val amountText: String = "",
    val paidBy: Long = 0L,
    val splitType: SplitType = SplitType.EQUAL,
    /** Who takes part in an equal split. Starts as everyone. */
    val selected: Set<Long> = emptySet(),
    /** member id -> typed amount in an exact split. */
    val exactTexts: Map<Long, String> = emptyMap(),
    val dateMillis: Long = System.currentTimeMillis(),
    val dateLabel: String = "",
    val note: String = "",
    val isSubmitting: Boolean = false,
    val errorRes: Int? = null,
    /** Extra detail for the error (e.g. how much of the bill is still unassigned), already formatted. */
    val errorArg: String? = null
) {
    val isEdit: Boolean get() = expenseId != null
}

sealed interface SplitExpenseFormUiEvent {
    data class Init(val groupId: Long, val expenseId: Long) : SplitExpenseFormUiEvent
    data class Title(val v: String) : SplitExpenseFormUiEvent
    data class Amount(val v: String) : SplitExpenseFormUiEvent
    data class PaidBy(val memberId: Long) : SplitExpenseFormUiEvent
    data class Type(val type: SplitType) : SplitExpenseFormUiEvent
    data class Toggle(val memberId: Long) : SplitExpenseFormUiEvent
    object SelectAll : SplitExpenseFormUiEvent
    data class Exact(val memberId: Long, val v: String) : SplitExpenseFormUiEvent
    data class Date(val millis: Long) : SplitExpenseFormUiEvent
    data class Note(val v: String) : SplitExpenseFormUiEvent
    object Save : SplitExpenseFormUiEvent
}

sealed interface SplitExpenseFormUiEffect {
    object NavigateBack : SplitExpenseFormUiEffect
}
