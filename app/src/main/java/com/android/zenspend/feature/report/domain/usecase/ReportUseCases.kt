package com.android.zenspend.feature.report.domain.usecase

import com.android.zenspend.feature.report.domain.model.*
import com.android.zenspend.feature.report.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFinancialOverviewUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    operator fun invoke(startDate: Long, endDate: Long, prevStartDate: Long? = null, prevEndDate: Long? = null): Flow<FinancialOverview> =
        repository.getFinancialOverview(startDate, endDate, prevStartDate, prevEndDate)
}

class GetCreditCardAnalysisUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    operator fun invoke(startDate: Long, endDate: Long): Flow<List<CreditCardReport>> =
        repository.getCreditCardAnalysis(startDate, endDate)
}

class GetEmiSummaryUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    operator fun invoke(): Flow<EMIReport> =
        repository.getEmiSummary()
}

class GetExpenseCategoryAnalysisUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    operator fun invoke(startDate: Long, endDate: Long): Flow<List<CategoryReport>> =
        repository.getExpenseCategoryAnalysis(startDate, endDate)
}

class GetIncomeCategoryAnalysisUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    operator fun invoke(startDate: Long, endDate: Long): Flow<List<CategoryReport>> =
        repository.getIncomeCategoryAnalysis(startDate, endDate)
}

class GetTrendAnalysisUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    operator fun invoke(startDate: Long, endDate: Long): Flow<List<TrendPoint>> =
        repository.getTrendAnalysis(startDate, endDate)
}

class GetWalletAnalysisUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    operator fun invoke(startDate: Long, endDate: Long): Flow<List<WalletActivityReport>> =
        repository.getWalletAnalysis(startDate, endDate)
}
