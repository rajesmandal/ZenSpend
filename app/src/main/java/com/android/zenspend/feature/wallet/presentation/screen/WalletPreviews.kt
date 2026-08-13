package com.android.zenspend.feature.wallet.presentation.screen

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.zenspend.core.database.model.WalletType
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.feature.wallet.presentation.model.WalletUiModel

@Preview(showBackground = true, name = "Wallet Item")
@Composable
fun WalletItemPreview() {
    ZenSpendTheme {
        Surface {
            WalletItem(
                name = "HDFC Bank",
                type = "BANK_ACCOUNT",
                balance = "25,000.00",
                isLiability = false,
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Credit Card Item")
@Composable
fun CreditCardItemPreview() {
    ZenSpendTheme {
        Surface {
            WalletItem(
                name = "ICICI Amazon Pay",
                type = "CREDIT_CARD",
                balance = "12,450.00",
                isLiability = true,
                onClick = {}
            )
        }
    }
}

@Preview(showSystemUi = true, name = "Balance Summary")
@Composable
fun BalanceSummaryPreview() {
    ZenSpendTheme {
        Surface {
            BalanceSummary(
                total = "12,550.00",
                assets = "25,000.00",
                liabilities = "12,450.00"
            )
        }
    }
}

@Preview(showSystemUi = true, name = "Detail Header")
@Composable
fun DetailHeaderPreview() {
    ZenSpendTheme {
        Surface {
            DetailHeader(
                name = "Main Savings",
                type = "BANK_ACCOUNT",
                balance = "52,000.00",
                isLiability = false
            )
        }
    }
}
