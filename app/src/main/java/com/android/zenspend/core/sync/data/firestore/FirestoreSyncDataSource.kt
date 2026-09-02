package com.android.zenspend.core.sync.data.firestore

import com.android.zenspend.core.database.entity.*
import com.android.zenspend.core.database.model.SyncState
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSyncDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun collection(householdId: String, type: String) =
        firestore.collection("households").document(householdId).collection(type)

    suspend fun uploadWallet(householdId: String, entity: WalletEntity) {
        val remoteId = entity.remoteId ?: return
        val data = mapOf(
            "remoteId" to remoteId,
            "householdId" to householdId,
            "name" to entity.name,
            "type" to entity.type.name,
            "openingBalance" to entity.openingBalance,
            "currency" to entity.currency,
            "isActive" to entity.isActive,
            "createdByUserId" to entity.createdByUserId,
            "updatedByUserId" to entity.updatedByUserId,
            "createdAt" to entity.createdAt,
            "updatedAt" to entity.updatedAt,
            "isDeleted" to entity.isDeleted
        )
        collection(householdId, "wallets").document(remoteId).set(data).await()
    }

    suspend fun uploadTransaction(householdId: String, entity: TransactionEntity, walletRemoteMap: Map<Long, String>, categoryRemoteMap: Map<Long, String>) {
        val remoteId = entity.remoteId ?: return
        val data = hashMapOf<String, Any?>(
            "remoteId" to remoteId,
            "householdId" to householdId,
            "title" to entity.title,
            "amount" to entity.amount,
            "date" to entity.date,
            "note" to entity.note,
            "type" to entity.type.name,
            "status" to entity.status.name,
            "source" to entity.source.name,
            "sourceWalletRemoteId" to entity.sourceWalletId?.let { walletRemoteMap[it] },
            "destinationWalletRemoteId" to entity.destinationWalletId?.let { walletRemoteMap[it] },
            "categoryRemoteId" to entity.categoryId?.let { categoryRemoteMap[it] },
            "emiRemoteId" to entity.emiId?.let { null },
            "recurringRemoteId" to entity.recurringTransactionId?.let { null },
            "recurringOccurrenceKey" to entity.recurringOccurrenceKey,
            "originalTransactionRemoteId" to entity.originalTransactionId?.let { null },
            "externalReference" to entity.externalReference,
            "isAutoDetected" to entity.isAutoDetected,
            "isUserConfirmed" to entity.isUserConfirmed,
            "isExcludedFromReports" to entity.isExcludedFromReports,
            "createdByUserId" to entity.createdByUserId,
            "updatedByUserId" to entity.updatedByUserId,
            "createdAt" to entity.createdAt,
            "updatedAt" to entity.updatedAt,
            "isDeleted" to entity.isDeleted
        )
        collection(householdId, "transactions").document(remoteId).set(data).await()
    }

    suspend fun uploadCategory(householdId: String, entity: CategoryEntity) {
        val remoteId = entity.remoteId ?: return
        collection(householdId, "categories").document(remoteId).set(mapOf(
            "remoteId" to remoteId,
            "householdId" to householdId,
            "name" to entity.name,
            "icon" to entity.icon,
            "color" to entity.color,
            "type" to entity.type.name,
            "isActive" to entity.isActive,
            "isDefault" to entity.isDefault,
            "createdByUserId" to entity.createdByUserId,
            "updatedByUserId" to entity.updatedByUserId,
            "createdAt" to entity.createdAt,
            "updatedAt" to entity.updatedAt,
            "isDeleted" to entity.isDeleted
        )).await()
    }

    suspend fun uploadBudget(householdId: String, entity: BudgetEntity, categoryRemoteMap: Map<Long, String>) {
        val remoteId = entity.remoteId ?: return
        collection(householdId, "budgets").document(remoteId).set(mapOf(
            "remoteId" to remoteId,
            "householdId" to householdId,
            "categoryRemoteId" to entity.categoryId?.let { categoryRemoteMap[it] },
            "amount" to entity.amount,
            "period" to entity.period.name,
            "startDate" to entity.startDate,
            "endDate" to entity.endDate,
            "isActive" to entity.isActive,
            "createdByUserId" to entity.createdByUserId,
            "updatedByUserId" to entity.updatedByUserId,
            "createdAt" to entity.createdAt,
            "updatedAt" to entity.updatedAt,
            "isDeleted" to entity.isDeleted
        )).await()
    }

    suspend fun uploadCreditCard(householdId: String, entity: CreditCardEntity, walletRemoteMap: Map<Long, String>) {
        val remoteId = entity.remoteId ?: return
        collection(householdId, "creditCards").document(remoteId).set(mapOf(
            "remoteId" to remoteId,
            "householdId" to householdId,
            "walletRemoteId" to walletRemoteMap[entity.walletId],
            "bankName" to entity.bankName,
            "lastFourDigits" to entity.lastFourDigits,
            "creditLimit" to entity.creditLimit,
            "billingCycleDate" to entity.billingCycleDate,
            "paymentDueDate" to entity.paymentDueDate,
            "active" to entity.active,
            "createdByUserId" to entity.createdByUserId,
            "updatedByUserId" to entity.updatedByUserId,
            "createdAt" to entity.createdAt,
            "updatedAt" to entity.updatedAt,
            "isDeleted" to entity.isDeleted
        )).await()
    }

    suspend fun uploadEmi(householdId: String, entity: EMIEntity, walletRemoteMap: Map<Long, String>) {
        val remoteId = entity.remoteId ?: return
        collection(householdId, "emis").document(remoteId).set(mapOf(
            "remoteId" to remoteId,
            "householdId" to householdId,
            "name" to entity.name,
            "walletRemoteId" to entity.walletId?.let { walletRemoteMap[it] },
            "principalAmount" to entity.principalAmount,
            "emiAmount" to entity.emiAmount,
            "interestRate" to entity.interestRate,
            "totalTenure" to entity.totalTenure,
            "remainingInstallments" to entity.remainingInstallments,
            "startDate" to entity.startDate,
            "endDate" to entity.endDate,
            "dueDay" to entity.dueDay,
            "status" to entity.status.name,
            "createdByUserId" to entity.createdByUserId,
            "updatedByUserId" to entity.updatedByUserId,
            "createdAt" to entity.createdAt,
            "updatedAt" to entity.updatedAt,
            "isDeleted" to entity.isDeleted
        )).await()
    }

    suspend fun uploadRecurring(householdId: String, entity: RecurringTransactionEntity, walletRemoteMap: Map<Long, String>, categoryRemoteMap: Map<Long, String>) {
        val remoteId = entity.remoteId ?: return
        collection(householdId, "recurringTransactions").document(remoteId).set(mapOf(
            "remoteId" to remoteId,
            "householdId" to householdId,
            "name" to entity.name,
            "amount" to entity.amount,
            "type" to entity.type.name,
            "recurrenceType" to entity.recurrenceType.name,
            "sourceWalletRemoteId" to entity.sourceWalletId?.let { walletRemoteMap[it] },
            "destinationWalletRemoteId" to entity.destinationWalletId?.let { walletRemoteMap[it] },
            "categoryRemoteId" to entity.categoryId?.let { categoryRemoteMap[it] },
            "startDate" to entity.startDate,
            "lastExecutedDate" to entity.lastExecutedDate,
            "nextExecutionDate" to entity.nextExecutionDate,
            "isActive" to entity.isActive,
            "note" to entity.note,
            "createdByUserId" to entity.createdByUserId,
            "updatedByUserId" to entity.updatedByUserId,
            "createdAt" to entity.createdAt,
            "updatedAt" to entity.updatedAt,
            "isDeleted" to entity.isDeleted
        )).await()
    }

    suspend fun downloadUpdated(
        householdId: String,
        collectionName: String,
        since: Long,
        batchSize: Long = 100
    ): List<Map<String, Any?>> {
        val snapshot = collection(householdId, collectionName)
            .whereGreaterThan("updatedAt", since)
            .orderBy("updatedAt")
            .limit(batchSize)
            .get()
            .await()
        return snapshot.documents.map { it.data ?: emptyMap() }
    }
}
