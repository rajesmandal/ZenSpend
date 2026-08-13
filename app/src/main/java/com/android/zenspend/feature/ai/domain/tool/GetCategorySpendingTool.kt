package com.android.zenspend.feature.ai.domain.tool

import com.android.zenspend.feature.ai.domain.util.DateResolver
import com.android.zenspend.feature.report.domain.repository.ReportRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class GetCategorySpendingTool @Inject constructor(
    private val reportRepository: ReportRepository
) : FinancialTool {
    override val name: String = "GetCategorySpending"
    override val description: String = "Get spending breakdown by category for a specific period."

    override suspend fun execute(parameters: Map<String, String>): ToolResult {
        val (start, end) = DateResolver.resolve(parameters[ToolParam.PERIOD])
        val analysis = reportRepository.getExpenseCategoryAnalysis(start, end).first()
        
        val categoryName = parameters[ToolParam.CATEGORY]
        val resultData = if (categoryName != null) {
            analysis.filter { it.categoryName.contains(categoryName, ignoreCase = true) }
        } else {
            analysis
        }.map { 
            CategorySpendingData(it.categoryName, it.amount)
        }
        
        return ToolResult(name, Json.encodeToString(resultData))
    }

    @Serializable
    private data class CategorySpendingData(
        val category: String,
        val amount: Long
    )
}
