package com.android.zenspend.core.ui.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.zenspend.core.ui.theme.ZenSpendTheme

@Preview(showBackground = true, name = "Primary Button Light")
@Composable
fun ZenPrimaryButtonPreview() {
    ZenSpendTheme(darkTheme = false) {
        Surface {
            ZenPrimaryButton(
                text = "Primary Button",
                onClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Primary Button Dark")
@Composable
fun ZenPrimaryButtonDarkPreview() {
    ZenSpendTheme(darkTheme = true) {
        Surface {
            ZenPrimaryButton(
                text = "Primary Button",
                onClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Amount Input")
@Composable
fun AmountInputPreview() {
    ZenSpendTheme {
        Surface {
            AmountInput(
                value = "1250.50",
                onValueChange = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Summary Card")
@Composable
fun SummaryCardPreview() {
    ZenSpendTheme {
        Surface {
            SummaryCard(
                title = "Total Balance",
                amount = "₹ 45,230.00",
                supportingText = "This month +₹ 2,000",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
fun LoadingStatePreview() {
    ZenSpendTheme {
        Surface {
            LoadingState()
        }
    }
}

@Preview(showBackground = true, name = "Empty State")
@Composable
fun EmptyStatePreview() {
    ZenSpendTheme {
        Surface {
            EmptyState(
                title = "No Transactions",
                message = "You haven't added any transactions yet. Tap the + button to start."
            )
        }
    }
}

@Preview(showBackground = true, name = "Zen Top App Bar")
@Composable
fun ZenTopAppBarPreview() {
    ZenSpendTheme {
        Surface {
            ZenTopAppBar(title = "Dashboard", subtitle = "Good morning")
        }
    }
}

@Preview(showBackground = true, name = "Amount Text")
@Composable
fun AmountTextPreview() {
    ZenSpendTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AmountText(amount = "₹12,450.00", type = AmountType.NEUTRAL, style = com.android.zenspend.core.ui.theme.AmountHero)
                AmountText(amount = "₹2,000.00", type = AmountType.INCOME)
                AmountText(amount = "₹850.00", type = AmountType.EXPENSE)
            }
        }
    }
}

@Preview(showBackground = true, name = "Zen Surface Card")
@Composable
fun ZenSurfaceCardPreview() {
    ZenSpendTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            ZenSurfaceCard {
                Text("Total Net Worth", style = MaterialTheme.typography.labelMedium)
                AmountText(amount = "₹45,230.00", type = AmountType.NEUTRAL)
            }
        }
    }
}

@Preview(showBackground = true, name = "Zen Segmented Control")
@Composable
fun ZenSegmentedControlPreview() {
    ZenSpendTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            ZenSegmentedControl(
                options = listOf("Personal", "Our Household"),
                selectedIndex = 0,
                onSelected = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Zen Status Banner")
@Composable
fun ZenStatusBannerPreview() {
    ZenSpendTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            ZenStatusBanner(message = "Synced with household")
        }
    }
}

@Preview(showBackground = true, name = "Zen Member Chip")
@Composable
fun ZenMemberChipPreview() {
    ZenSpendTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            ZenMemberChip(displayName = "Alex", role = "Owner", isCurrentUser = true)
        }
    }
}

@Preview(showBackground = true, name = "Zen Navigation Bar")
@Composable
fun ZenNavigationBarPreview() {
    ZenSpendTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ZenNavigationBar(
                selectedIndex = 0,
                items = listOf(
                    ZenNavItem("Home", com.android.zenspend.R.drawable.ic_nav_home),
                    ZenNavItem("History", com.android.zenspend.R.drawable.ic_nav_history),
                    ZenNavItem("Wallets", com.android.zenspend.R.drawable.ic_nav_wallets),
                    ZenNavItem("Insights", com.android.zenspend.R.drawable.ic_nav_insights),
                    ZenNavItem("More", com.android.zenspend.R.drawable.ic_nav_more)
                ),
                onItemSelected = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Zen Google Sign In")
@Composable
fun ZenGoogleSignInButtonPreview() {
    ZenSpendTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            ZenGoogleSignInButton(onClick = {})
        }
    }
}
