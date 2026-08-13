package com.android.zenspend.feature.backup.domain.usecase

import com.android.zenspend.core.database.model.CategoryType
import com.android.zenspend.core.database.model.TransactionSource
import com.android.zenspend.core.database.model.TransactionStatus
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.core.database.model.WalletType
import com.android.zenspend.core.util.ChecksumUtils
import com.android.zenspend.feature.backup.domain.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupSerializationTest {

    @Test
    fun `test backup envelope serialization and checksum validation`() {
        val data = BackupData(
            wallets = listOf(
                BackupWallet(1, "Cash", WalletType.CASH, 0, 100, "INR", true, 0, 0)
            ),
            categories = emptyList(),
            creditCards = emptyList(),
            emis = emptyList(),
            recurringTransactions = emptyList(),
            tags = emptyList(),
            transactions = listOf(
                BackupTransaction(
                    id = 1, title = "Lunch", amount = 50, date = 1000, note = null,
                    type = TransactionType.EXPENSE, status = TransactionStatus.COMPLETED,
                    source = TransactionSource.MANUAL, sourceWalletId = 1,
                    destinationWalletId = null, categoryId = null, emiId = null,
                    recurringTransactionId = null, recurringOccurrenceKey = null,
                    originalTransactionId = null, externalReference = null,
                    originalMessage = null, isAutoDetected = false, isUserConfirmed = true,
                    isExcludedFromReports = false, createdAt = 0, updatedAt = 0
                )
            ),
            transactionTags = emptyList(),
            attachments = emptyList(),
            budgets = emptyList()
        )

        val json = Json { prettyPrint = true }
        val dataJson = json.encodeToString(data)
        val checksum = ChecksumUtils.calculateSha256(dataJson)

        val metadata = BackupMetadata(1, "1.0", 0, "Test Device")
        val envelope = BackupEnvelope(metadata, data, checksum)
        
        val envelopeJson = json.encodeToString(envelope)
        
        // Deserialize
        val decodedEnvelope = Json.decodeFromString<BackupEnvelope>(envelopeJson)
        
        // Re-calculate checksum of decoded data
        val decodedDataJson = json.encodeToString(decodedEnvelope.data)
        val recalculatedChecksum = ChecksumUtils.calculateSha256(decodedDataJson)
        
        assertEquals(checksum, decodedEnvelope.checksum)
        assertEquals(checksum, recalculatedChecksum)
        assertEquals(1, decodedEnvelope.data.wallets.size)
        assertEquals("Lunch", decodedEnvelope.data.transactions[0].title)
    }
}
