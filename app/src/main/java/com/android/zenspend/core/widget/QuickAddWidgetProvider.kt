package com.android.zenspend.core.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.android.zenspend.MainActivity
import com.android.zenspend.R

/** Two buttons on the home screen that open the add-transaction screen. */
class QuickAddWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        val views = RemoteViews(context.packageName, R.layout.widget_quick_add)
        views.setOnClickPendingIntent(R.id.widget_income, openAdd(context, REQUEST_INCOME))
        views.setOnClickPendingIntent(R.id.widget_spent, openAdd(context, REQUEST_SPENT))
        manager.updateAppWidget(appWidgetIds, views)
    }

    private fun openAdd(context: Context, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = WidgetActions.ADD_TRANSACTION
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private companion object {
        const val REQUEST_INCOME = 11
        const val REQUEST_SPENT = 12
    }
}
