package com.android.zenspend.feature.ai.domain.tool

import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.ai.domain.util.DateResolver
import com.android.zenspend.feature.transaction.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class GetMerchantSpendingTool @Inject constructor(
    private val transactionRepository: TransactionRepository
) : FinancialTool {
    override val name: String = "GetMerchantSpending"
    override val description: String = "Get total spending at a specific merchant for a specific period."

    override suspend fun execute(parameters: Map<String, String>): ToolResult {
        val (start, end) = DateResolver.resolve(parameters[ToolParam.PERIOD])
        val merchantName = parameters[ToolParam.MERCHANT] ?: return ToolResult(name, "", ToolStatus.UNSUPPORTED_PARAMETERS, "Merchant name required")
        
        val transactions = transactionRepository.getFilteredTransactions(
            type = TransactionType.EXPENSE,
            startDate = start,
            endDate = end,
            searchQuery = merchantName
        ).first()
        
        val total = transactions.sumOf { it.amount }
        val resultData = MerchantSpendingData(merchantName, total, transactions.size)
        
        return ToolResult(name, Json.encodeToString(resultData))
    }

    @Serializable
    private data class MerchantSpendingData(
        val merchant: String,
        val totalAmount: Long,
        val count: Int
    )
}
