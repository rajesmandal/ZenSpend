package com.android.zenspend.feature.ai.domain.tool

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolRegistry @Inject constructor(
    private val getMonthlySummaryTool: GetMonthlySummaryTool,
    private val getCategorySpendingTool: GetCategorySpendingTool,
    private val getTopExpensesTool: GetTopExpensesTool,
    private val getWalletBalancesTool: GetWalletBalancesTool,
    private val getCreditCardSummaryTool: GetCreditCardSummaryTool,
    private val getEMISummaryTool: GetEMISummaryTool,
    private val getMerchantSpendingTool: GetMerchantSpendingTool
) {
    private val tools = listOf(
        getMonthlySummaryTool,
        getCategorySpendingTool,
        getTopExpensesTool,
        getWalletBalancesTool,
        getCreditCardSummaryTool,
        getEMISummaryTool,
        getMerchantSpendingTool
    ).associateBy { it.name }

    fun getTool(name: String): FinancialTool? = tools[name]
    
    fun getAllTools(): List<FinancialTool> = tools.values.toList()
}
