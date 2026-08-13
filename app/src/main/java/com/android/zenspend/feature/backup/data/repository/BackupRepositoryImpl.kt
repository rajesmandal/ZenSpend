package com.android.zenspend.feature.backup.data.repository

import androidx.room.withTransaction
import com.android.zenspend.core.database.AppDatabase
import com.android.zenspend.feature.backup.data.mapper.toBackup
import com.android.zenspend.feature.backup.data.mapper.toEntity
import com.android.zenspend.feature.backup.domain.model.BackupData
import com.android.zenspend.feature.backup.domain.repository.BackupRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class BackupRepositoryImpl @Inject constructor(
    private val database: AppDatabase
) : BackupRepository {

    private val backupDao = database.backupDao()

    override suspend fun getBackupData(): BackupData {
        return BackupData(
            wallets = backupDao.getAllWallets().map { it.toBackup() },
            categories = backupDao.getAllCategories().map { it.toBackup() },
            creditCards = backupDao.getAllCreditCards().map { it.toBackup() },
            emis = backupDao.getAllEmis().map { it.toBackup() },
            recurringTransactions = backupDao.getAllRecurringTransactions().map { it.toBackup() },
            tags = backupDao.getAllTags().map { it.toBackup() },
            transactions = backupDao.getAllTransactions().map { it.toBackup() },
            transactionTags = backupDao.getAllTransactionTags().map { it.toBackup() },
            attachments = backupDao.getAllAttachments().map { it.toBackup() },
            budgets = backupDao.getAllBudgets().map { it.toBackup() }
        )
    }

    override suspend fun restoreData(data: BackupData) {
        database.withTransaction {
            // Clear all data first
            database.clearAllTables()

            // Restore in dependency order
            backupDao.insertCategories(data.categories.map { it.toEntity() })
            backupDao.insertWallets(data.wallets.map { it.toEntity() })
            backupDao.insertTags(data.tags.map { it.toEntity() })
            
            backupDao.insertCreditCards(data.creditCards.map { it.toEntity() })
            backupDao.insertRecurringTransactions(data.recurringTransactions.map { it.toEntity() })
            backupDao.insertEmis(data.emis.map { it.toEntity() })
            backupDao.insertBudgets(data.budgets.map { it.toEntity() })
            
            backupDao.insertTransactions(data.transactions.map { it.toEntity() })
            
            backupDao.insertTransactionTags(data.transactionTags.map { it.toEntity() })
            backupDao.insertAttachments(data.attachments.map { it.toEntity() })
        }
    }

    override suspend fun clearAllData() {
        database.clearAllTables()
    }

    override suspend fun getTransactionsCsv(startDate: Long?, endDate: Long?): String {
        val rows = backupDao.getTransactionsForExport(startDate, endDate)
        val sb = StringBuilder()
        
        // Header
        sb.append("Date,Time,Title,Type,Amount,Category,Source Wallet,Destination Wallet,Note,Transaction ID\n")
        
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        
        rows.forEach { row ->
            val date = Date(row.date)
            sb.append("${dateFormatter.format(date)},")
            sb.append("${timeFormatter.format(date)},")
            sb.append("${escapeCsv(row.title)},")
            sb.append("${row.type},")
            sb.append("${row.amount},")
            sb.append("${escapeCsv(row.categoryName ?: "")},")
            sb.append("${escapeCsv(row.sourceWalletName ?: "")},")
            sb.append("${escapeCsv(row.destinationWalletName ?: "")},")
            sb.append("${escapeCsv(row.note ?: "")},")
            sb.append("${row.transactionId}\n")
        }
        
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"${value.replace("\"", "\"\"")}\""
        }
        return value
    }
}
