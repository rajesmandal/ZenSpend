package com.android.zenspend.core.navigation

sealed class Destination(val route: String) {
    object Dashboard : Destination("dashboard")
    
    object Wallets : Destination("wallets")
    object AddWallet : Destination("add_wallet")
    data class WalletDetails(val id: Long) : Destination("wallet_details/$id") {
        companion object {
            const val ROUTE = "wallet_details/{walletId}"
        }
    }
    data class EditWallet(val id: Long) : Destination("edit_wallet/$id") {
        companion object {
            const val ROUTE = "edit_wallet/{walletId}"
        }
    }

    object CreditCards : Destination("credit_cards")
    object AddCreditCard : Destination("add_credit_card")
    data class CreditCardDetails(val id: Long) : Destination("credit_card_details/$id") {
        companion object {
            const val ROUTE = "credit_card_details/{cardId}"
        }
    }

    object Emis : Destination("emis_list")
    data class AddEmi(val creditCardId: Long) : Destination("add_emi/$creditCardId") {
        companion object {
            const val ROUTE = "add_emi/{creditCardId}"
        }
    }

    data class EmiDetails(val id: Long) : Destination("emi_details/$id") {
        companion object {
            const val ROUTE = "emi_details/{emiId}"
        }
    }
    data class EditEmi(val id: Long) : Destination("edit_emi/$id") {
        companion object {
            const val ROUTE = "edit_emi/{emiId}"
        }
    }

    object Budgets : Destination("budgets_list")
    object AddBudget : Destination("add_budget")
    object BudgetPlan : Destination("budget_plan")
    data class BudgetDetails(val id: Long) : Destination("budget_details/$id") {
        companion object {
            const val ROUTE = "budget_details/{budgetId}"
        }
    }
    data class EditBudget(val id: Long) : Destination("edit_budget/$id") {
        companion object {
            const val ROUTE = "edit_budget/{budgetId}"
        }
    }

    object RecurringTransactions : Destination("recurring_transactions")
    object AddRecurringTransaction : Destination("add_recurring")
    data class RecurringDetails(val id: Long) : Destination("recurring_details/$id") {
        companion object {
            const val ROUTE = "recurring_details/{recurringId}"
        }
    }
    data class EditRecurring(val id: Long) : Destination("edit_recurring/$id") {
        companion object {
            const val ROUTE = "edit_recurring/{recurringId}"
        }
    }

    object Transactions : Destination("transactions")
    object AddTransaction : Destination("add_transaction")
    data class TransactionDetails(val id: Long) : Destination("transaction_details/$id") {
        companion object {
            const val ROUTE = "transaction_details/{transactionId}"
        }
    }
    data class EditTransaction(val id: Long) : Destination("edit_transaction/$id") {
        companion object {
            const val ROUTE = "edit_transaction/{transactionId}"
        }
    }

    object Categories : Destination("categories")
    object Reports : Destination("reports")
    object Settings : Destination("settings")
    object Backup : Destination("backup")
    object Premium : Destination("premium")
    object SmsDrafts : Destination("sms_drafts")
    object AiAssistant : Destination("ai_assistant")
    object Login : Destination("login")
    object SharedCashbook : Destination("shared_cashbook")
    data class SmsReview(val draftId: Long) : Destination("sms_review/$draftId") {
        companion object {
            const val ROUTE = "sms_review/{draftId}"
        }
    }
}
