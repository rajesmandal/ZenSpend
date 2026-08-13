package com.android.zenspend.feature.dashboard.domain.usecase

import com.android.zenspend.core.database.model.WalletType
import com.android.zenspend.feature.creditcard.domain.repository.CreditCardRepository
import com.android.zenspend.feature.dashboard.domain.model.*
import com.android.zenspend.feature.emi.domain.repository.EMIRepository
import com.android.zenspend.feature.transaction.domain.model.Transaction
import com.android.zenspend.feature.transaction.domain.repository.TransactionRepository
import com.android.zenspend.feature.wallet.domain.model.Wallet
import com.android.zenspend.feature.wallet.domain.repository.WalletRepository
import com.android.zenspend.feature.budget.domain.model.Budget
import com.android.zenspend.feature.budget.domain.repository.BudgetRepository
import com.android.zenspend.feature.recurring.domain.repository.RecurringTransactionRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class ObserveDashboardSummaryUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    private val creditCardRepository: CreditCardRepository,
    private val emiRepository: EMIRepository,
    private val budgetRepository: BudgetRepository,
    private val recurringRepository: RecurringTransactionRepository
) {
    operator fun invoke(startDate: Long, endDate: Long): Flow<DashboardSummary> {
        val walletsFlow = walletRepository.getAllActiveWallets()
        val incomeFlow = transactionRepository.getTotalIncome(startDate, endDate)
        val expenseFlow = transactionRepository.getTotalExpense(startDate, endDate)
        val categorySpendingFlow = transactionRepository.getCategorySpending(startDate, endDate)
        val ccCardsFlow = creditCardRepository.getAllActiveCreditCards()
        val emisFlow = emiRepository.getAllActiveEMIs()
        val transactionsFlow = transactionRepository.getFilteredTransactions(startDate = startDate, endDate = endDate)
        val budgetsFlow = budgetRepository.getAllActiveBudgets()
        val recurringFlow = recurringRepository.getAllActiveRecurringTransactions()

        return combine(
            combine(walletsFlow, incomeFlow, expenseFlow, categorySpendingFlow) { a, b, c, d -> listOf(a, b, c, d) },
            combine(ccCardsFlow, emisFlow, transactionsFlow, budgetsFlow) { a, b, c, d -> listOf(a, b, c, d) },
            recurringFlow
        ) { part1, part2, recurring ->
            val wallets = part1[0] as List<Wallet>
            val income = part1[1] as Long
            val expense = part1[2] as Long
            val categorySpending = part1[3] as List<CategorySpending>
            
            val ccCards = part2[Part2.CARDS] as List<com.android.zenspend.feature.creditcard.domain.model.CreditCard>
            val emis = part2[Part2.EMIS] as List<com.android.zenspend.feature.emi.domain.model.EMI>
            val transactions = part2[Part2.TXS] as List<com.android.zenspend.feature.transaction.domain.model.Transaction>
            val budgets = part2[Part2.BUDGETS] as List<Budget>

            // 1. Net Worth
            val assets = wallets.filter { it.type != WalletType.CREDIT_CARD }.sumOf { it.currentBalance }
            val ccWallets = wallets.filter { it.type == WalletType.CREDIT_CARD }
            val ccOutstanding = (ccWallets.sumOf { it.currentBalance }.coerceAtMost(0) * -1)
            
            val netWorth = assets - ccOutstanding

            // 3. Credit Card Summary
            val totalLimit = ccCards.sumOf { it.creditLimit }
            
            // 4. EMI Summary
            val nextEmi = emis.filter { it.status.name == "ACTIVE" }.minByOrNull { it.dueDay }
            
            // 5. Budget Summary
            val monthlyBudget = budgets.find { it.categoryId == null && it.startDate == startDate }
            val budgetProgress = monthlyBudget?.let { 
                com.android.zenspend.feature.budget.domain.model.BudgetProgress(it, expense)
            }

            // 6. Upcoming Recurring
            val upcomingRecurring = recurring.sortedBy { it.nextExecutionDate }.take(5)
            
            DashboardSummary(
                netWorth = NetWorthSummary(total = netWorth, assets = assets, liabilities = ccOutstanding),
                monthlySummary = MonthlyFlowSummary(income = income, expense = expense, savings = income - expense),
                topCategories = categorySpending.take(5),
                creditCardSummary = CreditCardDashboardSummary(
                    totalOutstanding = ccOutstanding,
                    totalAvailable = totalLimit - ccOutstanding,
                    activeCardsCount = ccCards.size
                ),
                upcomingEmi = nextEmi?.let { 
                    UpcomingEmiSummary(name = it.name, amount = it.emiAmount, dueDate = 0L)
                },
                monthlyBudget = budgetProgress,
                upcomingRecurring = upcomingRecurring,
                recentTransactions = transactions.take(10)
            )
        }
    }

    private object Part2 {
        const val CARDS = 0
        const val EMIS = 1
        const val TXS = 2
        const val BUDGETS = 3
    }
}
