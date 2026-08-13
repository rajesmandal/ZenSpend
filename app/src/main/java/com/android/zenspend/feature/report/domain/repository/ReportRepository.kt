package com.android.zenspend.feature.report.domain.repository

import com.android.zenspend.feature.report.domain.model.*
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    fun getFinancialOverview(startDate: Long, endDate: Long, prevStartDate: Long? = null, prevEndDate: Long? = null): Flow<FinancialOverview>
    fun getExpenseCategoryAnalysis(startDate: Long, endDate: Long): Flow<List<CategoryReport>>
    fun getIncomeCategoryAnalysis(startDate: Long, endDate: Long): Flow<List<CategoryReport>>
    fun getTrendAnalysis(startDate: Long, endDate: Long): Flow<List<TrendPoint>>
    fun getWalletAnalysis(startDate: Long, endDate: Long): Flow<List<WalletActivityReport>>
    fun getCreditCardAnalysis(startDate: Long, endDate: Long): Flow<List<CreditCardReport>>
    fun getEmiSummary(): Flow<EMIReport>
}
