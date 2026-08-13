package com.android.zenspend.feature.backup.domain.usecase

import android.os.Build
import com.android.zenspend.core.util.ChecksumUtils
import com.android.zenspend.feature.backup.domain.model.*
import com.android.zenspend.feature.backup.domain.repository.BackupRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class CreateBackupUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend operator fun invoke(outputStream: OutputStream, appVersion: String) {
        val data = repository.getBackupData()
        val metadata = BackupMetadata(
            schemaVersion = 1,
            appVersion = appVersion,
            createdAt = System.currentTimeMillis(),
            deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"
        )
        
        val json = Json { prettyPrint = true }
        val dataJson = json.encodeToString(data)
        val checksum = ChecksumUtils.calculateSha256(dataJson)
        
        val envelope = BackupEnvelope(metadata, data, checksum)
        val envelopeJson = json.encodeToString(envelope)
        
        outputStream.use { it.write(envelopeJson.toByteArray()) }
    }
}

class RestoreBackupUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    sealed class RestoreResult {
        object Success : RestoreResult()
        data class Error(val message: String) : RestoreResult()
    }

    suspend operator fun invoke(
        inputStream: InputStream,
        safetyBackupStream: OutputStream?,
        appVersion: String
    ): RestoreResult {
        return try {
            val json = Json { ignoreUnknownKeys = true }
            val envelopeJson = inputStream.bufferedReader().use { it.readText() }
            val envelope = json.decodeFromString<BackupEnvelope>(envelopeJson)
            
            // 1. Validate
            val dataJson = json.encodeToString(envelope.data)
            val calculatedChecksum = ChecksumUtils.calculateSha256(dataJson)
            
            if (calculatedChecksum != envelope.checksum) {
                return RestoreResult.Error("Integrity check failed: Checksum mismatch.")
            }
            
            if (envelope.metadata.schemaVersion > 1) {
                return RestoreResult.Error("Unsupported backup version: ${envelope.metadata.schemaVersion}")
            }

            // 2. Safety Backup
            if (safetyBackupStream == null) {
                return RestoreResult.Error("Safety backup failed: No output stream provided.")
            }
            
            try {
                val currentData = repository.getBackupData()
                val safetyMetadata = BackupMetadata(
                    schemaVersion = 1,
                    appVersion = appVersion,
                    createdAt = System.currentTimeMillis(),
                    deviceName = "Safety Backup"
                )
                val safetyDataJson = json.encodeToString(currentData)
                val safetyChecksum = ChecksumUtils.calculateSha256(safetyDataJson)
                val safetyEnvelope = BackupEnvelope(safetyMetadata, currentData, safetyChecksum)
                safetyBackupStream.use { it.write(json.encodeToString(safetyEnvelope).toByteArray()) }
            } catch (e: Exception) {
                return RestoreResult.Error("Restore aborted: Safety backup could not be created. ${e.message}")
            }

            // 3. Restore
            repository.restoreData(envelope.data)
            RestoreResult.Success
        } catch (e: Exception) {
            RestoreResult.Error("Restore failed: ${e.message}")
        }
    }
}

class GetBackupPreviewUseCase @Inject constructor() {
    data class BackupPreview(
        val createdAt: Long,
        val walletCount: Int,
        val transactionCount: Int,
        val categoryCount: Int,
        val creditCardCount: Int,
        val emiCount: Int,
        val budgetCount: Int,
        val recurringCount: Int
    )

    suspend operator fun invoke(inputStream: InputStream): BackupPreview? {
        return try {
            val json = Json { ignoreUnknownKeys = true }
            val envelopeJson = inputStream.bufferedReader().use { it.readText() }
            val envelope = json.decodeFromString<BackupEnvelope>(envelopeJson)
            val data = envelope.data
            
            BackupPreview(
                createdAt = envelope.metadata.createdAt,
                walletCount = data.wallets.size,
                transactionCount = data.transactions.size,
                categoryCount = data.categories.size,
                creditCardCount = data.creditCards.size,
                emiCount = data.emis.size,
                budgetCount = data.budgets.size,
                recurringCount = data.recurringTransactions.size
            )
        } catch (e: Exception) {
            null
        }
    }
}

class ExportTransactionsCsvUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend operator fun invoke(outputStream: OutputStream, startDate: Long? = null, endDate: Long? = null) {
        val csv = repository.getTransactionsCsv(startDate, endDate)
        outputStream.use { it.write(csv.toByteArray(Charsets.UTF_8)) }
    }
}
