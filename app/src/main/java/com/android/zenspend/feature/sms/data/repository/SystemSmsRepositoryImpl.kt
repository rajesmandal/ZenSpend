package com.android.zenspend.feature.sms.data.repository

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import com.android.zenspend.feature.sms.domain.model.SmsMessage
import com.android.zenspend.feature.sms.domain.repository.SystemSmsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SystemSmsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SystemSmsRepository {

    override suspend fun getHistoricalSms(limit: Int): List<SmsMessage> {
        val messages = mutableListOf<SmsMessage>()
        val uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf(Telephony.TextBasedSmsColumns.BODY, Telephony.TextBasedSmsColumns.ADDRESS, Telephony.TextBasedSmsColumns.DATE)
        
        try {
        context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            "${Telephony.TextBasedSmsColumns.DATE} DESC LIMIT $limit"
        )?.use { cursor ->
            val bodyIndex = cursor.getColumnIndex(Telephony.TextBasedSmsColumns.BODY)
            val addressIndex = cursor.getColumnIndex(Telephony.TextBasedSmsColumns.ADDRESS)
            val dateIndex = cursor.getColumnIndex(Telephony.TextBasedSmsColumns.DATE)
            
            while (cursor.moveToNext()) {
                val body = cursor.getString(bodyIndex)
                val address = cursor.getString(addressIndex)
                val date = cursor.getLong(dateIndex)
                messages.add(SmsMessage(body, address, date))
            }
        }
        } catch (e: SecurityException) {
            // READ_SMS not granted (yet): nothing to import.
        }

        return messages
    }
}
