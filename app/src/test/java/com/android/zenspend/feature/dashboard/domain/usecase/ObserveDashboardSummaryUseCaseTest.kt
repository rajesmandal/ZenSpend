package com.android.zenspend.feature.dashboard.domain.usecase

import com.android.zenspend.core.database.model.WalletType
import com.android.zenspend.feature.budget.domain.repository.BudgetRepository
import com.android.zenspend.feature.creditcard.domain.model.CreditCard
import com.android.zenspend.feature.creditcard.domain.repository.CreditCardRepository
import com.android.zenspend.feature.dashboard.domain.model.CategorySpending
import com.android.zenspend.feature.emi.domain.repository.EMIRepository
import com.android.zenspend.feature.transaction.domain.repository.TransactionRepository
import com.android.zenspend.feature.wallet.domain.model.Wallet
import com.android.zenspend.feature.wallet.domain.repository.WalletRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ObserveDashboardSummaryUseCaseTest {

    private val walletRepository: WalletRepository = mockk()
    private val transactionRepository: TransactionRepository = mockk()
    private val creditCardRepository: CreditCardRepository = mockk()
    private val emiRepository: EMIRepository = mockk()
    private val budgetRepository: BudgetRepository = mockk()
    private val recurringRepository: com.android.zenspend.feature.recurring.domain.repository.RecurringTransactionRepository = mockk()
    private lateinit var useCase: ObserveDashboardSummaryUseCase

    @Before
    fun setUp() {
        useCase = ObserveDashboardSummaryUseCase(
            walletRepository,
            transactionRepository,
            creditCardRepository,
            emiRepository,
            budgetRepository,
            recurringRepository
        )
    }

    @Test
    fun `when data is provided, should calculate net worth and summary correctly`() = runTest {
        // Setup
        val wallets = listOf(
            Wallet(id = 1, name = "Bank", type = WalletType.BANK, openingBalance = 0, currentBalance = 500000), // 5000
            Wallet(id = 2, name = "CC", type = WalletType.CREDIT_CARD, openingBalance = 0, currentBalance = -200000) // -2000
        )
        val ccCards = listOf(
            CreditCard(id = 1, walletId = 2, name = "Card", bankName = "HDFC", lastFourDigits = "1234", creditLimit = 1000000, billingCycleDate = 1, paymentDueDate = 15)
        )
        
        every { walletRepository.getAllActiveWallets() } returns flowOf(wallets)
        every { transactionRepository.getTotalIncome(any(), any()) } returns flowOf(100000L) // 1000
        every { transactionRepository.getTotalExpense(any(), any()) } returns flowOf(40000L) // 400
        every { transactionRepository.getCategorySpending(any(), any()) } returns flowOf(emptyList())
        every { creditCardRepository.getAllActiveCreditCards() } returns flowOf(ccCards)
        every { emiRepository.getAllActiveEMIs() } returns flowOf(emptyList())
        every { transactionRepository.getFilteredTransactions(any(), any(), any(), any(), any(), any()) } returns flowOf(emptyList())
        every { budgetRepository.getAllActiveBudgets() } returns flowOf(emptyList())
        every { recurringRepository.getAllActiveRecurringTransactions() } returns flowOf(emptyList())

        // Execute
        val summary = useCase(0, Long.MAX_VALUE).first()

        // Assert
        assertEquals(300000L, summary.netWorth.total) // 5000 - 2000 = 3000
        assertEquals(500000L, summary.netWorth.assets)
        assertEquals(200000L, summary.netWorth.liabilities)
        
        assertEquals(100000L, summary.monthlySummary.income)
        assertEquals(40000L, summary.monthlySummary.expense)
        assertEquals(60000L, summary.monthlySummary.savings)
    }
}
