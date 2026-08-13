package com.android.zenspend.feature.ai.domain.tool

import com.android.zenspend.feature.wallet.domain.repository.WalletRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class GetWalletBalancesTool @Inject constructor(
    private val walletRepository: WalletRepository
) : FinancialTool {
    override val name: String = "GetWalletBalances"
    override val description: String = "Get current balances of all active wallets."

    override suspend fun execute(parameters: Map<String, String>): ToolResult {
        val wallets = walletRepository.getAllActiveWallets().first()
        
        val walletName = parameters[ToolParam.WALLET_NAME]
        val balances = if (walletName != null) {
            wallets.filter { it.name.contains(walletName, ignoreCase = true) }
        } else {
            wallets
        }.map { 
            WalletBalanceData(it.name, it.currentBalance, it.type.name)
        }
        
        return ToolResult(name, Json.encodeToString(balances))
    }

    @Serializable
    private data class WalletBalanceData(
        val name: String,
        val balance: Long,
        val type: String
    )
}
