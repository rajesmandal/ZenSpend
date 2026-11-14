package com.android.zenspend.core.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.android.zenspend.MainActivity
import com.android.zenspend.R
import com.android.zenspend.core.ui.util.MoneyFormat
import com.android.zenspend.feature.transaction.domain.repository.TransactionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

/** This month's balance, income and spent. Refreshes about every 30 minutes (Android limits widget refresh rate). */
@AndroidEntryPoint
class MonthWidgetProvider : AppWidgetProvider() {

    @Inject lateinit var transactions: TransactionRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, manager, appWidgetIds)
        val pending = goAsync()
        scope.launch {
            try {
                val (start, end) = monthRange()
                val income = transactions.getTotalIncome(start, end).first()
                val spent = transactions.getTotalExpense(start, end).first()
                val views = RemoteViews(context.packageName, R.layout.widget_month)
                views.setTextViewText(
                    R.id.widget_month_title,
                    SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
                )
                views.setTextViewText(R.id.widget_balance, context.getString(R.string.amount_rupees, MoneyFormat.format(income - spent)))
                views.setTextViewText(
                    R.id.widget_income_total,
                    context.getString(R.string.widget_income_line, MoneyFormat.format(income))
                )
                views.setTextViewText(
                    R.id.widget_spent_total,
                    context.getString(R.string.widget_spent_line, MoneyFormat.format(spent))
                )
                views.setOnClickPendingIntent(
                    R.id.widget_root,
                    PendingIntent.getActivity(
                        context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
                    )
                )
                manager.updateAppWidget(appWidgetIds, views)
            } finally {
                pending.finish()
            }
        }
    }

    private fun monthRange(): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val end = (start.clone() as Calendar).apply { add(Calendar.MONTH, 1); add(Calendar.MILLISECOND, -1) }
        return start.timeInMillis to end.timeInMillis
    }
}
