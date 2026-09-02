package com.android.zenspend.core.navigation

object NavigationChrome {
    val topLevelRoutes = setOf(
        Destination.Dashboard.route,
        Destination.Transactions.route,
        Destination.Wallets.route,
        Destination.Reports.route,
        Destination.Settings.route
    )

    fun showsBottomBar(route: String?): Boolean {
        if (route == null) return false
        return topLevelRoutes.contains(route)
    }

    fun showsFab(route: String?): Boolean {
        if (route == null) return false
        return route == Destination.Dashboard.route || route == Destination.Transactions.route
    }

    fun fabDestination(route: String?): Destination {
        return when (route) {
            Destination.Wallets.route -> Destination.AddWallet
            else -> Destination.AddTransaction
        }
    }

    fun fabLabel(route: String?): String {
        return when (route) {
            Destination.Wallets.route -> "Wallet"
            else -> "Add"
        }
    }
}
