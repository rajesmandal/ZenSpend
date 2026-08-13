package com.android.zenspend.feature.transaction.presentation.screen

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.feature.transaction.presentation.model.TransactionUiModel

@Preview(showBackground = true, name = "Expense Item")
@Composable
fun ExpenseItemPreview() {
    ZenSpendTheme {
        Surface {
            TransactionItem(
                transaction = TransactionUiModel(
                    id = 1,
                    title = "Starbucks",
                    amount = "450.00",
                    date = "10 Aug 2026",
                    type = TransactionType.EXPENSE,
                    categoryName = "Food",
                    walletName = "Cash",
                    note = null
                ),
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Income Item")
@Composable
fun IncomeItemPreview() {
    ZenSpendTheme {
        Surface {
            TransactionItem(
                transaction = TransactionUiModel(
                    id = 2,
                    title = "Salary",
                    amount = "50,000.00",
                    date = "01 Aug 2026",
                    type = TransactionType.INCOME,
                    categoryName = "Salary",
                    walletName = "HDFC Bank",
                    note = null
                ),
                onClick = {}
            )
        }
    }
}
