package com.android.zenspend.core.ui.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
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
