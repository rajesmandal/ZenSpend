package com.android.zenspend.core.quickadd

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.android.zenspend.R
import com.android.zenspend.core.common.result.Resource
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.core.database.model.WalletType
import com.android.zenspend.core.ui.util.MoneyFormat
import com.android.zenspend.feature.transaction.domain.model.Transaction
import com.android.zenspend.feature.transaction.domain.usecase.AddTransactionUseCase
import com.android.zenspend.feature.wallet.domain.repository.WalletRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Saves what was typed into the notification, and puts the notification back after a reboot. */
@AndroidEntryPoint
class QuickAddReceiver : BroadcastReceiver() {

    @Inject lateinit var addTransaction: AddTransactionUseCase
    @Inject lateinit var walletRepository: WalletRepository
    @Inject lateinit var notifier: QuickAddNotifier

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val type = when (intent.action) {
            ACTION_INCOME -> TransactionType.INCOME
            ACTION_EXPENSE -> TransactionType.EXPENSE
            Intent.ACTION_BOOT_COMPLETED -> null
            else -> return
        }
        val typed = RemoteInput.getResultsFromIntent(intent)?.getCharSequence(KEY_TEXT)
        val pending = goAsync()
        scope.launch {
            try {
                if (type == null) notifier.sync() else save(context, type, typed)
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun save(context: Context, type: TransactionType, typed: CharSequence?) {
        val entry = QuickAddParser.parse(typed)
        if (entry == null) {
            notifier.show(context.getString(R.string.quick_add_bad_amount))
            return
        }
        // Everyday money lives in a normal wallet; a credit card is never the silent default.
        val wallet = walletRepository.getAllActiveWallets().first().firstOrNull { it.type != WalletType.CREDIT_CARD }
        if (wallet == null) {
            notifier.show(context.getString(R.string.quick_add_no_wallet))
            return
        }
        val income = type == TransactionType.INCOME
        val title = entry.title.ifBlank {
            context.getString(if (income) R.string.quick_add_default_income else R.string.quick_add_default_spent)
        }
        val result = addTransaction(
            Transaction(
                title = title,
                amount = entry.amountMinor,
                date = System.currentTimeMillis(),
                note = null,
                type = type,
                sourceWalletId = if (income) null else wallet.id,
                destinationWalletId = if (income) wallet.id else null
            )
        )
        val amount = MoneyFormat.format(entry.amountMinor)
        notifier.show(
            if (result is Resource.Success) {
                context.getString(
                    if (income) R.string.quick_add_done_income else R.string.quick_add_done_spent,
                    amount, wallet.name
                )
            } else {
                context.getString(R.string.quick_add_failed)
            }
        )
    }

    companion object {
        const val ACTION_INCOME = "com.android.zenspend.quickadd.INCOME"
        const val ACTION_EXPENSE = "com.android.zenspend.quickadd.EXPENSE"
        const val KEY_TEXT = "quick_add_text"
    }
}
