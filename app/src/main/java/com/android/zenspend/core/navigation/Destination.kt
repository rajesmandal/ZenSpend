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

    /** Add-card form pre-filled from what an SMS showed (bank may be unknown, so it is optional). */
    data class AddCreditCardSuggested(val last4: String, val bank: String?) :
        Destination("add_credit_card_suggested/$last4?bank=${android.net.Uri.encode(bank.orEmpty())}") {
        companion object {
            const val ROUTE = "add_credit_card_suggested/{last4}?bank={bank}"
        }
    }

    object People : Destination("people_list")
    data class PersonDetails(val id: Long) : Destination("person_details/$id") {
        companion object {
            const val ROUTE = "person_details/{personId}"
        }
    }
    data class AddLoan(val personId: Long) : Destination("add_loan/$personId") {
        companion object {
            const val ROUTE = "add_loan/{personId}"
        }
    }
    data class EditLoan(val loanId: Long) : Destination("edit_loan/$loanId") {
        companion object {
            const val ROUTE = "edit_loan/{loanId}"
        }
    }
    data class LoanDetails(val loanId: Long) : Destination("loan_details/$loanId") {
        companion object {
            const val ROUTE = "loan_details/{loanId}"
        }
    }

    object Splits : Destination("split_groups")
    data class SplitGroup(val id: Long) : Destination("split_group/$id") {
        companion object {
            const val ROUTE = "split_group/{groupId}"
        }
    }
    data class AddSplitExpense(val groupId: Long) : Destination("add_split_expense/$groupId") {
        companion object {
            const val ROUTE = "add_split_expense/{groupId}"
        }
    }
    data class EditSplitExpense(val expenseId: Long) : Destination("edit_split_expense/$expenseId") {
        companion object {
            const val ROUTE = "edit_split_expense/{expenseId}"
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
