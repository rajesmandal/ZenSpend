package com.android.zenspend.feature.sms.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.android.zenspend.core.datastore.AiPreferences
import com.android.zenspend.feature.sms.domain.usecase.SmsParserUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var smsParserUseCase: SmsParserUseCase

    @Inject
    lateinit var aiPreferences: com.android.zenspend.core.datastore.AiPreferences

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        // Without goAsync() the process can be killed as soon as onReceive returns,
        // before the message has been parsed and saved.
        val pending = goAsync()
        scope.launch {
            try {
                if (aiPreferences.isSmsDetectionEnabled.first()) {
                    val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                    // Multipart SMS arrive as several PDUs from the same sender: join them.
                    val sender = messages.firstOrNull()?.displayOriginatingAddress.orEmpty()
                    val body = messages.joinToString(separator = "") { it.displayMessageBody.orEmpty() }
                    val timestamp = messages.firstOrNull()?.timestampMillis ?: System.currentTimeMillis()
                    if (body.isNotBlank()) smsParserUseCase(body, sender, timestamp)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
