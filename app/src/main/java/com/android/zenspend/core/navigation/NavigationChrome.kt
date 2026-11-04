package com.android.zenspend.core.navigation

/**
 * Decides which "chrome" (dock, primary action) is shown for a route.
 * Keep navigation-shell rules here so the UI layer stays declarative.
 */
object NavigationChrome {
    /** Routes that live in the dock. Order == dock order. */
    val dockRoutes = listOf(
        Destination.Dashboard.route,
        Destination.Transactions.route,
        Destination.Wallets.route,
        Destination.Reports.route
    )

    /** Routes that show the dock but are not one of its tabs (reached from a header action). */
    private val extraTopLevelRoutes = setOf(Destination.Settings.route)

    val topLevelRoutes: Set<String> = dockRoutes.toSet() + extraTopLevelRoutes

    fun showsBottomBar(route: String?): Boolean {
        if (route == null) return false
        return topLevelRoutes.contains(route)
    }

    /** Destination opened by the dock's round "+" action on [route]. */
    fun primaryActionDestination(route: String?): Destination {
        return when (route) {
            Destination.Wallets.route -> Destination.AddWallet
            else -> Destination.AddTransaction
        }
    }
}
