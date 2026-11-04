package com.android.zenspend.feature.report.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.zenspend.R
import com.android.zenspend.core.ui.designsystem.component.ZenBadge
import com.android.zenspend.core.ui.designsystem.component.ZenProgressBar
import com.android.zenspend.core.ui.designsystem.component.ZenTile
import com.android.zenspend.core.ui.designsystem.component.ZenTileCaption
import com.android.zenspend.core.ui.designsystem.component.ZenTileValue
import com.android.zenspend.core.ui.theme.AmountLarge
import com.android.zenspend.core.ui.theme.AmountSmall
import com.android.zenspend.core.ui.theme.Dimensions
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.core.ui.theme.ZenTone
import com.android.zenspend.feature.report.domain.model.PeriodComparison
import com.android.zenspend.feature.report.domain.model.TrendPoint

@Composable
fun ReportSummarySection(
    income: String,
    expense: String,
    savings: String,
    savingsRate: String,
    comparison: PeriodComparison? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.TileGap)
    ) {
        SummaryRow {
            ReportMiniCard(
                label = stringResource(R.string.report_legend_income),
                amount = stringResource(R.string.amount_rupees, income),
                comparison = comparison?.incomeChange,
                tone = ZenTone.Lime,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            ReportMiniCard(
                label = stringResource(R.string.report_legend_expense),
                amount = stringResource(R.string.amount_rupees, expense),
                comparison = comparison?.expenseChange,
                tone = ZenTone.Coral,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
        SummaryRow {
            ReportMiniCard(
                label = stringResource(R.string.report_savings),
                amount = stringResource(R.string.amount_rupees, savings),
                comparison = comparison?.savingsChange,
                tone = ZenTone.Mint,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            ReportMiniCard(
                label = stringResource(R.string.report_savings_rate),
                amount = "$savingsRate%",
                tone = ZenTone.Lilac,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun SummaryRow(content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.TileGap),
        content = content
    )
}

@Composable
fun ReportMiniCard(
    label: String,
    amount: String,
    comparison: Float? = null,
    modifier: Modifier = Modifier,
    tone: ZenTone = ZenTone.Surface
) {
    ZenTile(modifier = modifier, tone = tone) {
        ZenTileCaption(label)
        Spacer(Modifier.height(Dimensions.SpacingXSmall))
        ZenTileValue(text = amount, style = AmountLarge)
        if (comparison != null) {
            val prefix = if (comparison >= 0) "+" else ""
            ZenTileCaption(
                text = "$prefix${"%.1f".format(comparison)}%",
                modifier = Modifier.padding(top = Dimensions.SpacingXSmall)
            )
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
    ZenTile(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ZenBadge(label = name, tone = ZenTone.forKey(name))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Dimensions.SpacingMedium)
            ) {
                Text(text = name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                if (transactionCount > 0) {
                    ZenTileCaption(stringResource(R.string.report_txs, transactionCount))
                }
            }
            ZenTileValue(
                text = stringResource(R.string.amount_rupees, amount),
                style = AmountSmall
            )
        }
        Spacer(Modifier.height(Dimensions.SpacingMedium))
        ZenProgressBar(progress = percentage / 100f)
        Spacer(Modifier.height(Dimensions.SpacingSmall))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ZenTileCaption(stringResource(R.string.report_of_total, "%.1f".format(percentage)))
            if (highestExpense != null) {
                ZenTileCaption(stringResource(R.string.report_largest, highestExpense))
            }
        }
    }
}

@Composable
fun TrendLineView(
    points: List<TrendPoint>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    val palette = ZenSpendTheme.colors
    val incomeColor = palette.tone(ZenTone.Lime).container
    val expenseColor = palette.tone(ZenTone.Coral).container

    ZenTile(modifier = modifier.fillMaxWidth(), tone = ZenTone.Ink) {
        Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.SpacingLarge)) {
            LegendDot(color = incomeColor, label = stringResource(R.string.report_legend_income))
            LegendDot(color = expenseColor, label = stringResource(R.string.report_legend_expense))
        }
        Spacer(Modifier.height(Dimensions.SpacingLarge))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            val maxAmount = points
                .flatMap { listOf(it.incomeAmount, it.expenseAmount) }
                .maxOrNull()?.coerceAtLeast(1L) ?: 1L
            val step = size.width / (points.size - 1).coerceAtLeast(1)

            fun linePath(selector: (TrendPoint) -> Long): Path = Path().apply {
                points.forEachIndexed { index, point ->
                    val x = index * step
                    val y = size.height - (selector(point).toFloat() / maxAmount * size.height)
                    if (index == 0) moveTo(x, y) else lineTo(x, y)
                }
            }

            val stroke = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            drawPath(linePath { it.incomeAmount }, color = incomeColor, style = stroke)
            drawPath(linePath { it.expenseAmount }, color = expenseColor, style = stroke)
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        ZenTileCaption(
            text = label,
            modifier = Modifier.padding(start = Dimensions.SpacingSmall)
        )
    }
}
