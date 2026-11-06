package com.android.zenspend.feature.recurring.presentation.viewmodel

import com.android.zenspend.core.database.model.RecurrenceType
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.recurring.domain.model.RecurringTransaction
import com.android.zenspend.feature.recurring.domain.usecase.*
import com.android.zenspend.feature.recurring.presentation.contract.RecurringUiEffect
import com.android.zenspend.feature.recurring.presentation.contract.RecurringUiEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecurringViewModelRestoreTest {

    private val observeUseCase: ObserveRecurringTransactionsUseCase = mockk()
    private val observeArchivedUseCase: ObserveArchivedRecurringTransactionsUseCase = mockk()
    private val restoreUseCase: RestoreRecurringTransactionUseCase = mockk()
    private val addUseCase: AddRecurringTransactionUseCase = mockk()
    private val updateUseCase: UpdateRecurringTransactionUseCase = mockk()
    private val statusUseCase: SetRecurringStatusUseCase = mockk()
    private val archiveUseCase: ArchiveRecurringTransactionUseCase = mockk()
    private val confirmUseCase: ConfirmRecurringOccurrenceUseCase = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { observeUseCase(any()) } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun rule(id: Long, active: Boolean = true) = RecurringTransaction(
        id = id, name = "Rent $id", amount = 1000, type = TransactionType.EXPENSE,
        recurrenceType = RecurrenceType.MONTHLY, sourceWalletId = 1, destinationWalletId = null,
        categoryId = null, startDate = 0, lastExecutedDate = null, nextExecutionDate = 0,
        isActive = active, note = null
    )

    private fun viewModel() = RecurringViewModel(
        observeUseCase, observeArchivedUseCase, addUseCase, updateUseCase,
        statusUseCase, archiveUseCase, restoreUseCase, confirmUseCase
    )

    @Test
    fun `archived rules are exposed in ui state`() = runTest {
        every { observeArchivedUseCase() } returns flowOf(listOf(rule(7, active = false)))

        val vm = viewModel()

        assertEquals(listOf(7L), vm.uiState.value.archivedTransactions.map { it.id })
    }

    @Test
    fun `restore event calls use case and shows snackbar`() = runTest {
        every { observeArchivedUseCase() } returns flowOf(emptyList())
        coEvery { restoreUseCase(7) } returns Unit
        val vm = viewModel()

        vm.handleEvent(RecurringUiEvent.Restore(7))

        coVerify(exactly = 1) { restoreUseCase(7) }
        val effect = vm.uiEffect.first()
        assertTrue(effect is RecurringUiEffect.ShowSnackbar)
    }

    @Test
    fun `restore failure shows error snackbar instead of crashing`() = runTest {
        every { observeArchivedUseCase() } returns flowOf(emptyList())
        coEvery { restoreUseCase(7) } throws IllegalStateException("db closed")
        val vm = viewModel()

        vm.handleEvent(RecurringUiEvent.Restore(7))

        val effect = vm.uiEffect.first()
        assertTrue(effect is RecurringUiEffect.ShowSnackbar)
    }
}
