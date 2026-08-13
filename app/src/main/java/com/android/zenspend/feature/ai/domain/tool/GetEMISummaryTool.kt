package com.android.zenspend.feature.ai.domain.tool

import com.android.zenspend.feature.report.domain.repository.ReportRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class GetEMISummaryTool @Inject constructor(
    private val reportRepository: ReportRepository
) : FinancialTool {
    override val name: String = "GetEMISummary"
    override val description: String = "Get summary of active EMIs and remaining debt."

    override suspend fun execute(parameters: Map<String, String>): ToolResult {
        val summary = reportRepository.getEmiSummary().first()
        
        val resultData = EmiSummaryData(
            activeCount = summary.activeEmiCount,
            monthlyBurden = summary.monthlyBurden,
            totalRemaining = summary.totalRemainingObligation
        )
        
        return ToolResult(name, Json.encodeToString(resultData))
    }

    @Serializable
    private data class EmiSummaryData(
        val activeCount: Int,
        val monthlyBurden: Long,
        val totalRemaining: Long
    )
}
