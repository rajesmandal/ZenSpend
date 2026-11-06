package com.android.zenspend.feature.sms.data.dao

import androidx.room.*
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.sms.data.entity.SmsDraftEntity
import com.android.zenspend.feature.sms.data.entity.SmsDraftStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsDraftDao {
    @Query("SELECT * FROM sms_drafts WHERE status = :status ORDER BY date DESC")
    fun getDraftsByStatus(status: SmsDraftStatus = SmsDraftStatus.PENDING): Flow<List<SmsDraftEntity>>

    @Query("SELECT * FROM sms_drafts WHERE fingerprint = :fingerprint")
    suspend fun getDraftByFingerprint(fingerprint: String): SmsDraftEntity?

    /** Status-agnostic: a confirmed or ignored draft must still block its late duplicate. */
    @Query(
        "SELECT COUNT(*) FROM sms_drafts WHERE sender = :sender AND amount = :amount " +
            "AND type = :type AND date BETWEEN :from AND :to"
    )
    suspend fun countSimilarDrafts(
        sender: String,
        amount: Long,
        type: TransactionType,
        from: Long,
        to: Long
    ): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDraft(draft: SmsDraftEntity): Long

    @Update
    suspend fun updateDraft(draft: SmsDraftEntity)

    @Query("UPDATE sms_drafts SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: SmsDraftStatus)

    @Query("DELETE FROM sms_drafts WHERE status = 'IGNORED' AND createdAt < :timestamp")
    suspend fun cleanupOldIgnoredDrafts(timestamp: Long)
}
