package com.android.zenspend.core.quickadd

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import com.android.zenspend.MainActivity
import com.android.zenspend.R
import com.android.zenspend.core.datastore.QuickAddPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The pinned "+ Income / − Spent" notification. Each button opens an inline text box in the
 * shade, so an entry is saved without opening the app.
 */
@Singleton
class QuickAddNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: QuickAddPreferences
) {
    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /** Shows or removes the notification to match the saved setting. */
    suspend fun sync() {
        if (preferences.isNotificationEnabled.first()) show() else cancel()
    }

    /** [status] is a one-line result of the last entry ("Added ₹250 …"), shown under the title. */
    fun show(status: String? = null) {
        if (!canNotify()) return
        ensureChannel()
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_quick_add)
            .setContentTitle(context.getString(R.string.quick_add_title))
            .setContentText(status ?: context.getString(R.string.quick_add_hint))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentIntent(open)
            .addAction(action(QuickAddReceiver.ACTION_INCOME, R.string.quick_add_income, REQUEST_INCOME))
            .addAction(action(QuickAddReceiver.ACTION_EXPENSE, R.string.quick_add_spent, REQUEST_EXPENSE))
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    fun cancel() = manager.cancel(NOTIFICATION_ID)

    private fun action(intentAction: String, labelRes: Int, requestCode: Int): NotificationCompat.Action {
        val intent = Intent(context, QuickAddReceiver::class.java).setAction(intentAction)
        // Mutable on purpose: the system fills the typed text into the RemoteInput result.
        val pending = PendingIntent.getBroadcast(
            context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        val input = RemoteInput.Builder(QuickAddReceiver.KEY_TEXT)
            .setLabel(context.getString(R.string.quick_add_input_hint))
            .build()
        return NotificationCompat.Action.Builder(0, context.getString(labelRes), pending)
            .addRemoteInput(input)
            .setAllowGeneratedReplies(false)
            .build()
    }

    private fun canNotify(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.quick_add_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply { setShowBadge(false) }
            manager.createNotificationChannel(channel)
        }
    }

    private companion object {
        const val CHANNEL_ID = "quick_add"
        const val NOTIFICATION_ID = 4101
        const val REQUEST_INCOME = 1
        const val REQUEST_EXPENSE = 2
    }
}
