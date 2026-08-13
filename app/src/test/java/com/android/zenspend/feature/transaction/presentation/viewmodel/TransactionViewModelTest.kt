package com.android.zenspend.feature.transaction.presentation.viewmodel

import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.category.domain.repository.CategoryRepository
import com.android.zenspend.feature.transaction.domain.repository.TransactionRepository
import com.android.zenspend.feature.transaction.domain.usecase.*
import com.android.zenspend.feature.transaction.presentation.contract.TransactionUiEvent
import com.android.zenspend.feature.wallet.domain.usecase.ObserveWalletsUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModelTest {

    private val repository: TransactionRepository = mockk()
    private val addTransactionUseCase: AddTransactionUseCase = mockk()
    private val updateTransactionUseCase: UpdateTransactionUseCase = mockk()
    private val deleteTransactionUseCase: DeleteTransactionUseCase = mockk()
    private val observeWalletsUseCase: ObserveWalletsUseCase = mockk()
    private val categoryRepository: CategoryRepository = mockk()
    private val getCategorySuggestionUseCase: com.android.zenspend.feature.category.domain.usecase.GetCategorySuggestionUseCase = mockk()
    private val updateMerchantPreferenceUseCase: com.android.zenspend.feature.category.domain.usecase.UpdateMerchantPreferenceUseCase = mockk()
    
    private lateinit var viewModel: TransactionViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getFilteredTransactions(any(), any(), any(), any(), any(), any()) } returns flowOf(emptyList())
        every { observeWalletsUseCase() } returns flowOf(emptyList())
        every { categoryRepository.getAllActiveCategories() } returns flowOf(emptyList())
        
        viewModel = TransactionViewModel(
            repository,
            addTransactionUseCase,
            updateTransactionUseCase,
            deleteTransactionUseCase,
            observeWalletsUseCase,
            categoryRepository,
            getCategorySuggestionUseCase,
            updateMerchantPreferenceUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should load transactions`() = runTest {
        val state = viewModel.uiState.value
        assertEquals(0, state.transactions.size)
    }

    @Test
    fun `type change should update form state`() = runTest {
        viewModel.handleEvent(TransactionUiEvent.OnTypeChange(TransactionType.INCOME))
        assertEquals(TransactionType.INCOME, viewModel.uiState.value.formState.type)
    }

    @Test
    fun `invalid amount should set error`() = runTest {
        viewModel.handleEvent(TransactionUiEvent.OnAmountChange("0"))
        viewModel.handleEvent(TransactionUiEvent.SaveTransaction)
        assertEquals("Invalid amount", viewModel.uiState.value.formState.amountError)
    }
}
