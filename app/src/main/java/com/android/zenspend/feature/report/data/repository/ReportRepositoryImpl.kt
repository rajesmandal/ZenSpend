package com.android.zenspend.feature.report.data.repository

import com.android.zenspend.core.database.dao.TransactionDao
import com.android.zenspend.core.database.dao.CreditCardDao
import com.android.zenspend.core.database.dao.EMIDao
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.report.domain.model.*
import com.android.zenspend.feature.report.domain.repository.ReportRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val creditCardDao: CreditCardDao,
    private val emiDao: EMIDao
) : ReportRepository {

    override fun getFinancialOverview(startDate: Long, endDate: Long, prevStartDate: Long?, prevEndDate: Long?): Flow<FinancialOverview> {
        val currentFlow = combine(
            transactionDao.getTotalAmountByType(TransactionType.INCOME, startDate, endDate),
            transactionDao.getTotalNetExpense(TransactionType.EXPENSE, TransactionType.REFUND, startDate, endDate)
        ) { income, expense -> income to expense }

        val previousFlow: Flow<Pair<Long, Long>?> = if (prevStartDate != null && prevEndDate != null) {
            combine(
                transactionDao.getTotalAmountByType(TransactionType.INCOME, prevStartDate, prevEndDate),
                transactionDao.getTotalNetExpense(TransactionType.EXPENSE, TransactionType.REFUND, prevStartDate, prevEndDate)
            ) { income, expense -> income to expense }
        } else {
            flowOf(null)
        }

        return combine(currentFlow, previousFlow) { current, previous ->
            val (income, expense) = current
            val savings = income - expense
            val savingsRate = if (income > 0) (savings.toFloat() / income) * 100f else 0f

            val comparison = previous?.let { (prevIncome, prevExpense) ->
                val prevSavings = prevIncome - prevExpense
                PeriodComparison(
                    incomeChange = calculatePercentageChange(prevIncome, income),
                    expenseChange = calculatePercentageChange(prevExpense, expense),
                    savingsChange = calculatePercentageChange(prevSavings, savings)
                )
            }

            FinancialOverview(
                totalIncome = income,
                totalExpense = expense,
                savings = savings,
                savingsRate = savingsRate,
                comparison = comparison
            )
        }
    }

    private fun calculatePercentageChange(old: Long, new: Long): Float {
        if (old == 0L) return if (new > 0L) 100f else 0f
        return ((new - old).toFloat() / old) * 100f
    }

    override fun getExpenseCategoryAnalysis(startDate: Long, endDate: Long): Flow<List<CategoryReport>> {
        return transactionDao.getCategorySpending(startDate, endDate).map { entities ->
            val total = entities.sumOf { it.totalAmount }
            entities.map { entity ->
                CategoryReport(
                    categoryId = entity.categoryId,
                    categoryName = entity.categoryName,
                    amount = entity.totalAmount,
                    percentage = if (total > 0) (entity.totalAmount.toFloat() / total) * 100f else 0f,
                    transactionCount = entity.transactionCount,
                    highestExpense = entity.maxAmount
                )
            }
        }
    }

    override fun getIncomeCategoryAnalysis(startDate: Long, endDate: Long): Flow<List<CategoryReport>> {
        return transactionDao.getCategoryIncome(startDate, endDate).map { entities ->
            val total = entities.sumOf { it.totalAmount }
            entities.map { entity ->
                CategoryReport(
                    categoryId = entity.categoryId,
                    categoryName = entity.categoryName,
                    amount = entity.totalAmount,
                    percentage = if (total > 0) (entity.totalAmount.toFloat() / total) * 100f else 0f,
                    transactionCount = entity.transactionCount,
                    highestExpense = entity.maxAmount
                )
            }
        }
    }

    override fun getTrendAnalysis(startDate: Long, endDate: Long): Flow<List<TrendPoint>> {
        return transactionDao.getDailyTrend(startDate, endDate).map { entities ->
            entities.map {
                TrendPoint(
                    date = it.date,
                    incomeAmount = it.incomeAmount,
                    expenseAmount = it.expenseAmount
                )
            }
        }
    }

    override fun getWalletAnalysis(startDate: Long, endDate: Long): Flow<List<WalletActivityReport>> {
        return transactionDao.getWalletActivity(startDate, endDate).map { entities ->
            entities.map {
                WalletActivityReport(
                    walletId = it.walletId,
                    walletName = it.walletName,
                    incomeAmount = it.incomeAmount,
                    expenseAmount = it.expenseAmount,
                    netMovement = it.incomeAmount - it.expenseAmount
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getCreditCardAnalysis(startDate: Long, endDate: Long): Flow<List<CreditCardReport>> {
        return creditCardDao.getAllActiveCreditCards().flatMapLatest { cards ->
            if (cards.isEmpty()) return@flatMapLatest flowOf(emptyList<CreditCardReport>())
            val flows = cards.map { card ->
                transactionDao.getCalculatedBalance(card.walletId).map { balance ->
                    val outstanding = balance?.let { -it } ?: 0L
                    CreditCardReport(
                        cardId = card.id,
                        cardName = card.bankName,
                        spending = 0,
                        payments = 0,
                        outstanding = outstanding,
                        utilization = if (card.creditLimit > 0) (outstanding.toFloat() / card.creditLimit) * 100f else 0f
                    )
                }
            }
            combine(flows) { it.toList() }
        }
    }

    override fun getEmiSummary(): Flow<EMIReport> {
        return emiDao.getEMIsByStatus().map { emis ->
            val active = emis.filter { it.status == com.android.zenspend.core.database.model.EMIStatus.ACTIVE }
            EMIReport(
                activeEmiCount = active.size,
                monthlyBurden = active.sumOf { it.emiAmount },
                totalRemainingObligation = active.sumOf { it.emiAmount * it.remainingInstallments }
            )
        }
    }
}
