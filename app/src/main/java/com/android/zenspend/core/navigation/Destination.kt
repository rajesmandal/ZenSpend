package com.android.zenspend.core.navigation

sealed class Destination(val route: String) {
    object Dashboard : Destination("dashboard")
    object Transactions : Destination("transactions")
    object Wallets : Destination("wallets")
    object Categories : Destination("categories")
    object Budgets : Destination("budgets")
    object Emis : Destination("emis")
    object Reports : Destination("reports")
    object Settings : Destination("settings")
    object Backup : Destination("backup")
    object Premium : Destination("premium")
}
