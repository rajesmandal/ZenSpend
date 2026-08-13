package com.android.zenspend.feature.budget.presentation.screen

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.feature.budget.domain.model.Budget
import com.android.zenspend.feature.budget.domain.model.BudgetProgress

@Preview(showBackground = true, name = "Budget Card")
@Composable
fun BudgetOverviewCardPreview() {
    ZenSpendTheme {
        Surface {
            BudgetOverviewCard(
                progress = BudgetProgress(
                    budget = Budget(amount = 5000000, categoryId = null, startDate = 0, endDate = 0),
                    spentAmount = 3200000
                )
            )
        }
    }
}

@Preview(showBackground = true, name = "Over Budget Card")
@Composable
fun OverBudgetCardPreview() {
    ZenSpendTheme {
        Surface {
            BudgetOverviewCard(
                progress = BudgetProgress(
                    budget = Budget(amount = 5000000, categoryId = null, startDate = 0, endDate = 0),
                    spentAmount = 6200000
                )
            )
        }
    }
}
