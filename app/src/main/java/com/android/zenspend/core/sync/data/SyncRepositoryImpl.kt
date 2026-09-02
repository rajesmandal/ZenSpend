package com.android.zenspend.core.sync.data

import com.android.zenspend.core.auth.domain.repository.AuthRepository
import com.android.zenspend.core.database.dao.SyncCursorDao
import com.android.zenspend.core.database.dao.SyncQueueDao
import com.android.zenspend.core.database.entity.*
import com.android.zenspend.core.database.model.*
import com.android.zenspend.core.sync.data.firestore.FirestoreSyncDataSource
import com.android.zenspend.core.sync.domain.model.InitialSyncProgress
import com.android.zenspend.core.sync.domain.repository.SyncRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val syncQueueDao: SyncQueueDao,
    private val syncCursorDao: SyncCursorDao,
    private val firestoreSync: FirestoreSyncDataSource,
    private val authRepository: AuthRepository,
    private val remoteChangeGuard: RemoteChangeGuard
) : SyncRepository {

    val isApplyingRemoteChange: Boolean get() = remoteChangeGuard.isApplyingRemoteChange

    override suspend fun hasPendingChanges(): Boolean {
        return syncQueueDao.getPendingWallets().isNotEmpty() ||
            syncQueueDao.getPendingTransactions().isNotEmpty() ||
            syncQueueDao.getPendingCategories().isNotEmpty() ||
            syncQueueDao.getPendingBudgets().isNotEmpty() ||
            syncQueueDao.getPendingCreditCards().isNotEmpty() ||
            syncQueueDao.getPendingEmis().isNotEmpty() ||
            syncQueueDao.getPendingRecurring().isNotEmpty()
    }

    override suspend fun uploadPendingChanges(householdId: String, userId: String) {
        val walletRemoteMap = mutableMapOf<Long, String>()

        syncQueueDao.getPendingWallets().forEach { wallet ->
            if (wallet.householdId == null) return@forEach
            val withRemote = ensureRemoteId(wallet, userId)
            if (withRemote.remoteId != wallet.remoteId || withRemote.updatedAt != wallet.updatedAt) {
                syncQueueDao.updateWallet(withRemote)
            }
            try {
                firestoreSync.uploadWallet(householdId, withRemote)
                syncQueueDao.updateWallet(withRemote.copy(syncState = SyncState.SYNCED))
                walletRemoteMap[withRemote.id] = withRemote.remoteId!!
            } catch (_: Exception) {
                syncQueueDao.updateWallet(withRemote.copy(syncState = SyncState.FAILED))
            }
        }

        syncQueueDao.getPendingWallets().filter { it.syncState == SyncState.SYNCED && it.remoteId != null }
            .forEach { walletRemoteMap[it.id] = it.remoteId!! }

        val categoryRemoteMap = mutableMapOf<Long, String>()
        syncQueueDao.getPendingCategories().forEach { category ->
            if (category.householdId == null) return@forEach
            val withRemote = ensureRemoteId(category, userId)
            try {
                firestoreSync.uploadCategory(householdId, withRemote)
                syncQueueDao.updateCategory(withRemote.copy(syncState = SyncState.SYNCED))
                categoryRemoteMap[withRemote.id] = withRemote.remoteId!!
            } catch (_: Exception) {
                syncQueueDao.updateCategory(withRemote.copy(syncState = SyncState.FAILED))
            }
        }
        syncQueueDao.getPendingCategories().filter { it.syncState == SyncState.SYNCED && it.remoteId != null }
            .forEach { categoryRemoteMap[it.id] = it.remoteId!! }

        syncQueueDao.getPendingCreditCards().forEach { card ->
            if (card.householdId == null) return@forEach
            val withRemote = ensureRemoteId(card, userId)
            try {
                firestoreSync.uploadCreditCard(householdId, withRemote, walletRemoteMap)
                syncQueueDao.updateCreditCard(withRemote.copy(syncState = SyncState.SYNCED))
            } catch (_: Exception) {
                syncQueueDao.updateCreditCard(withRemote.copy(syncState = SyncState.FAILED))
            }
        }

        syncQueueDao.getPendingEmis().forEach { emi ->
            if (emi.householdId == null) return@forEach
            val withRemote = ensureRemoteId(emi, userId)
            try {
                firestoreSync.uploadEmi(householdId, withRemote, walletRemoteMap)
                syncQueueDao.updateEmi(withRemote.copy(syncState = SyncState.SYNCED))
            } catch (_: Exception) {
                syncQueueDao.updateEmi(withRemote.copy(syncState = SyncState.FAILED))
            }
        }

        syncQueueDao.getPendingRecurring().forEach { recurring ->
            if (recurring.householdId == null) return@forEach
            val withRemote = ensureRemoteId(recurring, userId)
            try {
                firestoreSync.uploadRecurring(householdId, withRemote, walletRemoteMap, categoryRemoteMap)
                syncQueueDao.updateRecurring(withRemote.copy(syncState = SyncState.SYNCED))
            } catch (_: Exception) {
                syncQueueDao.updateRecurring(withRemote.copy(syncState = SyncState.FAILED))
            }
        }

        syncQueueDao.getPendingBudgets().forEach { budget ->
            if (budget.householdId == null) return@forEach
            val withRemote = ensureRemoteId(budget, userId)
            try {
                firestoreSync.uploadBudget(householdId, withRemote, categoryRemoteMap)
                syncQueueDao.updateBudget(withRemote.copy(syncState = SyncState.SYNCED))
            } catch (_: Exception) {
                syncQueueDao.updateBudget(withRemote.copy(syncState = SyncState.FAILED))
            }
        }

        syncQueueDao.getPendingTransactions().forEach { tx ->
            if (tx.householdId == null) return@forEach
            val withRemote = ensureRemoteId(tx, userId)
            try {
                firestoreSync.uploadTransaction(householdId, withRemote, walletRemoteMap, categoryRemoteMap)
                syncQueueDao.updateTransaction(withRemote.copy(syncState = SyncState.SYNCED))
            } catch (_: Exception) {
                syncQueueDao.updateTransaction(withRemote.copy(syncState = SyncState.FAILED))
            }
        }
    }

    override suspend fun downloadIncrementalChanges(householdId: String) {
        remoteChangeGuard.isApplyingRemoteChange = true
        try {
            val entityTypes = listOf(
                "wallets", "categories", "creditCards", "emis",
                "recurringTransactions", "budgets", "transactions"
            )
            entityTypes.forEach { type ->
                val cursor = syncCursorDao.getCursor(householdId, type)?.lastRemoteUpdatedAt ?: 0L
                val docs = firestoreSync.downloadUpdated(householdId, type, cursor)
                var maxUpdated = cursor
                docs.forEach { data ->
                    val updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: 0L
                    if (updatedAt > maxUpdated) maxUpdated = updatedAt
                    applyRemoteDocument(type, householdId, data)
                }
                syncCursorDao.upsertCursor(
                    SyncCursorEntity(householdId = householdId, entityType = type, lastRemoteUpdatedAt = maxUpdated)
                )
            }
        } finally {
            remoteChangeGuard.isApplyingRemoteChange = false
        }
    }

    override suspend fun performInitialSync(
        householdId: String,
        onProgress: (InitialSyncProgress) -> Unit
    ) {
        val entityTypes = listOf(
            "wallets", "categories", "creditCards", "emis",
            "recurringTransactions", "budgets", "transactions"
        )
        entityTypes.forEachIndexed { index, type ->
            var since = 0L
            var batchCount = 0
            var hasMore = true
            while (hasMore) {
                val docs = firestoreSync.downloadUpdated(householdId, type, since, 100)
                remoteChangeGuard.isApplyingRemoteChange = true
                try {
                    docs.forEach { applyRemoteDocument(type, householdId, it) }
                } finally {
                    remoteChangeGuard.isApplyingRemoteChange = false
                }
                batchCount += docs.size
                since = docs.maxOfOrNull { (it["updatedAt"] as? Number)?.toLong() ?: 0L } ?: since
                hasMore = docs.size >= 100
                onProgress(
                    InitialSyncProgress(
                        entityType = type,
                        downloaded = batchCount,
                        total = null,
                        isComplete = !hasMore && index == entityTypes.lastIndex
                    )
                )
            }

            syncCursorDao.upsertCursor(
                SyncCursorEntity(householdId = householdId, entityType = type, lastRemoteUpdatedAt = since)
            )
        }
    }

    private suspend fun applyRemoteDocument(type: String, householdId: String, data: Map<String, Any?>) {
        when (type) {
            "wallets" -> applyRemoteWallet(householdId, data)
            "categories" -> applyRemoteCategory(householdId, data)
            "transactions" -> applyRemoteTransaction(householdId, data)
            "budgets" -> applyRemoteBudget(householdId, data)
            "creditCards" -> applyRemoteCreditCard(householdId, data)
            "emis" -> applyRemoteEmi(householdId, data)
            "recurringTransactions" -> applyRemoteRecurring(householdId, data)
        }
    }

    private suspend fun applyRemoteWallet(householdId: String, data: Map<String, Any?>) {
        val remoteId = data["remoteId"] as? String ?: return
        val existing = syncQueueDao.getWalletByRemoteId(remoteId)
        val updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        if (existing != null && existing.updatedAt >= updatedAt) return

        val entity = WalletEntity(
            id = existing?.id ?: 0,
            name = data["name"] as? String ?: return,
            type = WalletType.valueOf(data["type"] as? String ?: WalletType.CASH.name),
            openingBalance = (data["openingBalance"] as? Number)?.toLong() ?: 0L,
            currency = data["currency"] as? String ?: "INR",
            isActive = data["isActive"] as? Boolean ?: true,
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: updatedAt,
            updatedAt = updatedAt,
            remoteId = remoteId,
            householdId = householdId,
            createdByUserId = data["createdByUserId"] as? String,
            updatedByUserId = data["updatedByUserId"] as? String,
            isDeleted = data["isDeleted"] as? Boolean ?: false,
            syncState = SyncState.SYNCED
        )
        if (existing == null) syncQueueDao.insertWallet(entity) else syncQueueDao.updateWallet(entity)
    }

    private suspend fun applyRemoteCategory(householdId: String, data: Map<String, Any?>) {
        val remoteId = data["remoteId"] as? String ?: return
        val existing = syncQueueDao.getCategoryByRemoteId(remoteId)
        val updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        if (existing != null && existing.updatedAt >= updatedAt) return

        val entity = CategoryEntity(
            id = existing?.id ?: 0,
            name = data["name"] as? String ?: return,
            icon = data["icon"] as? String,
            color = data["color"] as? String,
            type = CategoryType.valueOf(data["type"] as? String ?: CategoryType.EXPENSE.name),
            isActive = data["isActive"] as? Boolean ?: true,
            isDefault = data["isDefault"] as? Boolean ?: false,
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: updatedAt,
            updatedAt = updatedAt,
            remoteId = remoteId,
            householdId = householdId,
            createdByUserId = data["createdByUserId"] as? String,
            updatedByUserId = data["updatedByUserId"] as? String,
            isDeleted = data["isDeleted"] as? Boolean ?: false,
            syncState = SyncState.SYNCED
        )
        if (existing == null) syncQueueDao.insertCategory(entity) else syncQueueDao.updateCategory(entity)
    }

    private suspend fun applyRemoteTransaction(householdId: String, data: Map<String, Any?>) {
        val remoteId = data["remoteId"] as? String ?: return
        val existing = syncQueueDao.getTransactionByRemoteId(remoteId)
        val updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        if (existing != null && existing.updatedAt >= updatedAt && existing.syncState == SyncState.SYNCED) return

        val sourceWalletRemoteId = data["sourceWalletRemoteId"] as? String
        val destWalletRemoteId = data["destinationWalletRemoteId"] as? String
        val categoryRemoteId = data["categoryRemoteId"] as? String

        val entity = TransactionEntity(
            id = existing?.id ?: 0,
            title = data["title"] as? String ?: return,
            amount = (data["amount"] as? Number)?.toLong() ?: 0L,
            date = (data["date"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            note = data["note"] as? String,
            type = TransactionType.valueOf(data["type"] as? String ?: TransactionType.EXPENSE.name),
            status = TransactionStatus.valueOf(data["status"] as? String ?: TransactionStatus.COMPLETED.name),
            source = TransactionSource.valueOf(data["source"] as? String ?: TransactionSource.MANUAL.name),
            sourceWalletId = sourceWalletRemoteId?.let { syncQueueDao.getWalletByRemoteId(it)?.id },
            destinationWalletId = destWalletRemoteId?.let { syncQueueDao.getWalletByRemoteId(it)?.id },
            categoryId = categoryRemoteId?.let { syncQueueDao.getCategoryByRemoteId(it)?.id },
            recurringOccurrenceKey = data["recurringOccurrenceKey"] as? String,
            externalReference = data["externalReference"] as? String,
            isAutoDetected = data["isAutoDetected"] as? Boolean ?: false,
            isUserConfirmed = data["isUserConfirmed"] as? Boolean ?: true,
            isExcludedFromReports = data["isExcludedFromReports"] as? Boolean ?: false,
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: updatedAt,
            updatedAt = updatedAt,
            remoteId = remoteId,
            householdId = householdId,
            createdByUserId = data["createdByUserId"] as? String,
            updatedByUserId = data["updatedByUserId"] as? String,
            isDeleted = data["isDeleted"] as? Boolean ?: false,
            syncState = SyncState.SYNCED
        )
        if (existing == null) syncQueueDao.insertTransaction(entity) else syncQueueDao.updateTransaction(entity)
    }

    private suspend fun applyRemoteBudget(householdId: String, data: Map<String, Any?>) {
        val remoteId = data["remoteId"] as? String ?: return
        val existing = syncQueueDao.getBudgetByRemoteId(remoteId)
        val updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        if (existing != null && existing.updatedAt >= updatedAt) return
        val categoryRemoteId = data["categoryRemoteId"] as? String
        val entity = BudgetEntity(
            id = existing?.id ?: 0,
            categoryId = categoryRemoteId?.let { syncQueueDao.getCategoryByRemoteId(it)?.id },
            amount = (data["amount"] as? Number)?.toLong() ?: 0L,
            period = BudgetPeriod.valueOf(data["period"] as? String ?: BudgetPeriod.MONTHLY.name),
            startDate = (data["startDate"] as? Number)?.toLong() ?: 0L,
            endDate = (data["endDate"] as? Number)?.toLong() ?: 0L,
            isActive = data["isActive"] as? Boolean ?: true,
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: updatedAt,
            updatedAt = updatedAt,
            remoteId = remoteId,
            householdId = householdId,
            createdByUserId = data["createdByUserId"] as? String,
            updatedByUserId = data["updatedByUserId"] as? String,
            isDeleted = data["isDeleted"] as? Boolean ?: false,
            syncState = SyncState.SYNCED
        )
        if (existing == null) syncQueueDao.insertBudget(entity) else syncQueueDao.updateBudget(entity)
    }

    private suspend fun applyRemoteCreditCard(householdId: String, data: Map<String, Any?>) {
        val remoteId = data["remoteId"] as? String ?: return
        val existing = syncQueueDao.getCreditCardByRemoteId(remoteId)
        val updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        if (existing != null && existing.updatedAt >= updatedAt) return
        val walletRemoteId = data["walletRemoteId"] as? String ?: return
        val walletId = syncQueueDao.getWalletByRemoteId(walletRemoteId)?.id ?: return
        val entity = CreditCardEntity(
            id = existing?.id ?: 0,
            walletId = walletId,
            bankName = data["bankName"] as? String ?: return,
            lastFourDigits = data["lastFourDigits"] as? String,
            creditLimit = (data["creditLimit"] as? Number)?.toLong() ?: 0L,
            billingCycleDate = (data["billingCycleDate"] as? Number)?.toInt() ?: 1,
            paymentDueDate = (data["paymentDueDate"] as? Number)?.toInt() ?: 1,
            active = data["active"] as? Boolean ?: true,
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: updatedAt,
            updatedAt = updatedAt,
            remoteId = remoteId,
            householdId = householdId,
            createdByUserId = data["createdByUserId"] as? String,
            updatedByUserId = data["updatedByUserId"] as? String,
            isDeleted = data["isDeleted"] as? Boolean ?: false,
            syncState = SyncState.SYNCED
        )
        if (existing == null) syncQueueDao.insertCreditCard(entity) else syncQueueDao.updateCreditCard(entity)
    }

    private suspend fun applyRemoteEmi(householdId: String, data: Map<String, Any?>) {
        val remoteId = data["remoteId"] as? String ?: return
        val existing = syncQueueDao.getEmiByRemoteId(remoteId)
        val updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        if (existing != null && existing.updatedAt >= updatedAt) return
        val walletRemoteId = data["walletRemoteId"] as? String
        val entity = EMIEntity(
            id = existing?.id ?: 0,
            name = data["name"] as? String ?: return,
            walletId = walletRemoteId?.let { syncQueueDao.getWalletByRemoteId(it)?.id },
            principalAmount = (data["principalAmount"] as? Number)?.toLong() ?: 0L,
            emiAmount = (data["emiAmount"] as? Number)?.toLong() ?: 0L,
            interestRate = (data["interestRate"] as? Number)?.toDouble(),
            totalTenure = (data["totalTenure"] as? Number)?.toInt() ?: 0,
            remainingInstallments = (data["remainingInstallments"] as? Number)?.toInt() ?: 0,
            startDate = (data["startDate"] as? Number)?.toLong() ?: 0L,
            endDate = (data["endDate"] as? Number)?.toLong() ?: 0L,
            dueDay = (data["dueDay"] as? Number)?.toInt() ?: 1,
            status = EMIStatus.valueOf(data["status"] as? String ?: EMIStatus.ACTIVE.name),
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: updatedAt,
            updatedAt = updatedAt,
            remoteId = remoteId,
            householdId = householdId,
            createdByUserId = data["createdByUserId"] as? String,
            updatedByUserId = data["updatedByUserId"] as? String,
            isDeleted = data["isDeleted"] as? Boolean ?: false,
            syncState = SyncState.SYNCED
        )
        if (existing == null) syncQueueDao.insertEmi(entity) else syncQueueDao.updateEmi(entity)
    }

    private suspend fun applyRemoteRecurring(householdId: String, data: Map<String, Any?>) {
        val remoteId = data["remoteId"] as? String ?: return
        val existing = syncQueueDao.getRecurringByRemoteId(remoteId)
        val updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        if (existing != null && existing.updatedAt >= updatedAt) return
        val entity = RecurringTransactionEntity(
            id = existing?.id ?: 0,
            name = data["name"] as? String ?: return,
            amount = (data["amount"] as? Number)?.toLong() ?: 0L,
            type = TransactionType.valueOf(data["type"] as? String ?: TransactionType.EXPENSE.name),
            recurrenceType = RecurrenceType.valueOf(data["recurrenceType"] as? String ?: RecurrenceType.MONTHLY.name),
            sourceWalletId = (data["sourceWalletRemoteId"] as? String)?.let { syncQueueDao.getWalletByRemoteId(it)?.id },
            destinationWalletId = (data["destinationWalletRemoteId"] as? String)?.let { syncQueueDao.getWalletByRemoteId(it)?.id },
            categoryId = (data["categoryRemoteId"] as? String)?.let { syncQueueDao.getCategoryByRemoteId(it)?.id },
            startDate = (data["startDate"] as? Number)?.toLong() ?: 0L,
            lastExecutedDate = (data["lastExecutedDate"] as? Number)?.toLong(),
            nextExecutionDate = (data["nextExecutionDate"] as? Number)?.toLong() ?: 0L,
            isActive = data["isActive"] as? Boolean ?: true,
            note = data["note"] as? String,
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: updatedAt,
            updatedAt = updatedAt,
            remoteId = remoteId,
            householdId = householdId,
            createdByUserId = data["createdByUserId"] as? String,
            updatedByUserId = data["updatedByUserId"] as? String,
            isDeleted = data["isDeleted"] as? Boolean ?: false,
            syncState = SyncState.SYNCED
        )
        if (existing == null) syncQueueDao.insertRecurring(entity) else syncQueueDao.updateRecurring(entity)
    }

    private fun ensureRemoteId(entity: WalletEntity, userId: String): WalletEntity {
        val remoteId = entity.remoteId ?: UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        return entity.copy(
            remoteId = remoteId,
            updatedAt = now,
            updatedByUserId = userId,
            createdByUserId = entity.createdByUserId ?: userId
        )
    }

    private fun ensureRemoteId(entity: CategoryEntity, userId: String): CategoryEntity {
        val remoteId = entity.remoteId ?: UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        return entity.copy(remoteId = remoteId, updatedAt = now, updatedByUserId = userId, createdByUserId = entity.createdByUserId ?: userId)
    }

    private fun ensureRemoteId(entity: TransactionEntity, userId: String): TransactionEntity {
        val remoteId = entity.remoteId ?: UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        return entity.copy(remoteId = remoteId, updatedAt = now, updatedByUserId = userId, createdByUserId = entity.createdByUserId ?: userId)
    }

    private fun ensureRemoteId(entity: BudgetEntity, userId: String): BudgetEntity {
        val remoteId = entity.remoteId ?: UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        return entity.copy(remoteId = remoteId, updatedAt = now, updatedByUserId = userId, createdByUserId = entity.createdByUserId ?: userId)
    }

    private fun ensureRemoteId(entity: CreditCardEntity, userId: String): CreditCardEntity {
        val remoteId = entity.remoteId ?: UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        return entity.copy(remoteId = remoteId, updatedAt = now, updatedByUserId = userId, createdByUserId = entity.createdByUserId ?: userId)
    }

    private fun ensureRemoteId(entity: EMIEntity, userId: String): EMIEntity {
        val remoteId = entity.remoteId ?: UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        return entity.copy(remoteId = remoteId, updatedAt = now, updatedByUserId = userId, createdByUserId = entity.createdByUserId ?: userId)
    }

    private fun ensureRemoteId(entity: RecurringTransactionEntity, userId: String): RecurringTransactionEntity {
        val remoteId = entity.remoteId ?: UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        return entity.copy(remoteId = remoteId, updatedAt = now, updatedByUserId = userId, createdByUserId = entity.createdByUserId ?: userId)
    }
}
