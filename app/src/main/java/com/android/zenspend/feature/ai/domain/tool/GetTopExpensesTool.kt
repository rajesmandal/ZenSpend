package com.android.zenspend.feature.ai.domain.tool

import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.ai.domain.util.DateResolver
import com.android.zenspend.feature.transaction.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class GetTopExpensesTool @Inject constructor(
    private val transactionRepository: TransactionRepository
) : FinancialTool {
    override val name: String = "GetTopExpenses"
    override val description: String = "Get the largest expenses for a specific period."

    override suspend fun execute(parameters: Map<String, String>): ToolResult {
        val (start, end) = DateResolver.resolve(parameters[ToolParam.PERIOD])
        val transactions = transactionRepository.getFilteredTransactions(
            type = TransactionType.EXPENSE,
            startDate = start,
            endDate = end
        ).first()
        
        val topExpenses = transactions
            .sortedByDescending { it.amount }
            .take(5)
            .map { ExpenseData(it.title, it.amount, it.date) }
        
        return ToolResult(name, Json.encodeToString(topExpenses))
    }

    @Serializable
    private data class ExpenseData(
        val title: String,
        val amount: Long,
        val date: Long
    )
}
