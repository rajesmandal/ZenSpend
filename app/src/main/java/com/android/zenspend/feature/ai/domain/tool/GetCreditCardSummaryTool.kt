package com.android.zenspend.feature.ai.domain.tool

import com.android.zenspend.feature.report.domain.repository.ReportRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class GetCreditCardSummaryTool @Inject constructor(
    private val reportRepository: ReportRepository
) : FinancialTool {
    override val name: String = "GetCreditCardSummary"
    override val description: String = "Get outstanding balance and utilization for credit cards."

    override suspend fun execute(parameters: Map<String, String>): ToolResult {
        // We use current month for analysis context if needed, but CC analysis usually gives current state
        val analysis = reportRepository.getCreditCardAnalysis(0, System.currentTimeMillis()).first()
        
        val summary = analysis.map { 
            CardSummaryData(it.cardName, it.outstanding, it.utilization)
        }
        
        return ToolResult(name, Json.encodeToString(summary))
    }

    @Serializable
    private data class CardSummaryData(
        val name: String,
        val outstanding: Long,
        val utilization: Float
    )
}
