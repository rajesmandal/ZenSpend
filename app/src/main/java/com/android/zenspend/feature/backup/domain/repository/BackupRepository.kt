package com.android.zenspend.feature.backup.domain.repository

import com.android.zenspend.feature.backup.domain.model.BackupData
import com.android.zenspend.feature.backup.domain.model.BackupEnvelope

interface BackupRepository {
    suspend fun getBackupData(): BackupData
    suspend fun restoreData(data: BackupData)
    suspend fun clearAllData()
    suspend fun getTransactionsCsv(startDate: Long?, endDate: Long?): String
}
