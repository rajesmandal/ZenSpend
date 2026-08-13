package com.android.zenspend.feature.report.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.zenspend.core.ui.theme.Dimensions

@Composable
fun ReportSummarySection(
    income: String,
    expense: String,
    savings: String,
    savingsRate: String,
    comparison: com.android.zenspend.feature.report.domain.model.PeriodComparison? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
        ) {
            ReportMiniCard(
                label = "Income", 
                amount = "₹$income", 
                comparison = comparison?.incomeChange,
                modifier = Modifier.weight(1f)
            )
            ReportMiniCard(
                label = "Expense", 
                amount = "₹$expense", 
                comparison = comparison?.expenseChange,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
        ) {
            ReportMiniCard(
                label = "Savings", 
                amount = "₹$savings", 
                comparison = comparison?.savingsChange,
                modifier = Modifier.weight(1f)
            )
            ReportMiniCard(
                label = "Savings Rate", 
                amount = "$savingsRate%", 
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ReportMiniCard(
    label: String,
    amount: String,
    comparison: Float? = null,
    modifier: Modifier = Modifier
) {
    com.android.zenspend.core.ui.designsystem.component.ZenCard(modifier = modifier) {
        Column(modifier = Modifier.padding(Dimensions.SpacingMedium)) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(amount, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (comparison != null) {
                val color = if (comparison >= 0) com.android.zenspend.core.ui.theme.IncomeGreen else com.android.zenspend.core.ui.theme.ExpenseRed
                val prefix = if (comparison >= 0) "+" else ""
                Text(
                    text = "$prefix${"%.1f".format(comparison)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = color
                )
            }
        }
    }
}

@Composable
fun CategoryReportItem(
    name: String,
    amount: String,
    percentage: Float,
    transactionCount: Int = 0,
    highestExpense: String? = null,
    modifier: Modifier = Modifier
) {
    com.android.zenspend.core.ui.designsystem.component.ZenCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimensions.SpacingMedium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("₹$amount", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(Dimensions.SpacingXSmall))
            
            LinearProgressIndicator(
                progress = { percentage / 100f },
                modifier = Modifier.fillMaxWidth(),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            
            Spacer(modifier = Modifier.height(Dimensions.SpacingXSmall))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${"%.1f".format(percentage)}% of total",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (transactionCount > 0) {
                    Text(
                        text = "$transactionCount Txs",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (highestExpense != null) {
                Text(
                    text = "Largest: ₹$highestExpense",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun TrendLineView(
    points: List<com.android.zenspend.feature.report.domain.model.TrendPoint>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return
    
    com.android.zenspend.core.ui.designsystem.component.ZenCard(modifier = modifier.fillMaxWidth().height(200.dp)) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val maxAmount = points.flatMap { listOf(it.incomeAmount, it.expenseAmount) }.maxOrNull()?.coerceAtLeast(1L) ?: 1L
            val width = size.width
            val height = size.height
            val step = width / (points.size - 1).coerceAtLeast(1)

            val incomePath = androidx.compose.ui.graphics.Path()
            val expensePath = androidx.compose.ui.graphics.Path()

            points.forEachIndexed { index, point ->
                val x = index * step
                val yIncome = height - (point.incomeAmount.toFloat() / maxAmount * height)
                val yExpense = height - (point.expenseAmount.toFloat() / maxAmount * height)

                if (index == 0) {
                    incomePath.moveTo(x, yIncome)
                    expensePath.moveTo(x, yExpense)
                } else {
                    incomePath.lineTo(x, yIncome)
                    expensePath.lineTo(x, yExpense)
                }
            }

            drawPath(incomePath, color = com.android.zenspend.core.ui.theme.IncomeGreen, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))
            drawPath(expensePath, color = com.android.zenspend.core.ui.theme.ExpenseRed, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))
        }
    }
}
