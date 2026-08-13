package com.android.zenspend.feature.ai.domain.tool

import com.android.zenspend.feature.ai.domain.util.DateResolver
import com.android.zenspend.feature.report.domain.repository.ReportRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class GetMonthlySummaryTool @Inject constructor(
    private val reportRepository: ReportRepository
) : FinancialTool {
    override val name: String = "GetMonthlySummary"
    override val description: String = "Get total income, expense, and savings for a specific period."

    override suspend fun execute(parameters: Map<String, String>): ToolResult {
        val (start, end) = DateResolver.resolve(parameters[ToolParam.PERIOD])
        val overview = reportRepository.getFinancialOverview(start, end).first()
        
        val summary = SummaryData(
            income = overview.totalIncome,
            expense = overview.totalExpense,
            savings = overview.savings,
            period = parameters[ToolParam.PERIOD] ?: "this_month"
        )
        
        return ToolResult(name, Json.encodeToString(summary))
    }

    @Serializable
    private data class SummaryData(
        val income: Long,
        val expense: Long,
        val savings: Long,
        val period: String
    )
}
