package com.android.zenspend.feature.budget.domain.usecase

import com.android.zenspend.feature.budget.domain.model.Budget
import com.android.zenspend.feature.budget.domain.repository.BudgetRepository
import com.android.zenspend.feature.category.domain.repository.CategoryRepository
import com.android.zenspend.feature.transaction.domain.repository.TransactionRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ObserveBudgetsWithProgressUseCaseTest {

    private val budgetRepository: BudgetRepository = mockk()
    private val transactionRepository: TransactionRepository = mockk()
    private val categoryRepository: CategoryRepository = mockk()
    private lateinit var useCase: ObserveBudgetsWithProgressUseCase

    @Before
    fun setUp() {
        useCase = ObserveBudgetsWithProgressUseCase(
            budgetRepository,
            transactionRepository,
            categoryRepository
        )
    }

    @Test
    fun `when overall budget exists, should calculate progress correctly`() = runTest {
        val budgets = listOf(
            Budget(id = 1, categoryId = null, amount = 100000, startDate = 0, endDate = 1000)
        )
        every { budgetRepository.getAllActiveBudgets() } returns flowOf(budgets)
        every { transactionRepository.getTotalExpense(any(), any()) } returns flowOf(60000L)
        
        val result = useCase(0, 1000).first()
        
        assertEquals(1, result.size)
        assertEquals(60000L, result[0].spentAmount)
        assertEquals(40000L, result[0].remainingAmount)
        assertEquals(0.6f, result[0].progress)
    }
}
