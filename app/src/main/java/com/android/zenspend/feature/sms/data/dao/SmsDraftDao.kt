package com.android.zenspend.feature.sms.data.dao

import androidx.room.*
import com.android.zenspend.feature.sms.data.entity.SmsDraftEntity
import com.android.zenspend.feature.sms.data.entity.SmsDraftStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsDraftDao {
    @Query("SELECT * FROM sms_drafts WHERE status = :status ORDER BY date DESC")
    fun getDraftsByStatus(status: SmsDraftStatus = SmsDraftStatus.PENDING): Flow<List<SmsDraftEntity>>

    @Query("SELECT * FROM sms_drafts WHERE fingerprint = :fingerprint")
    suspend fun getDraftByFingerprint(fingerprint: String): SmsDraftEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDraft(draft: SmsDraftEntity): Long

    @Update
    suspend fun updateDraft(draft: SmsDraftEntity)

    @Query("UPDATE sms_drafts SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: SmsDraftStatus)

    @Query("DELETE FROM sms_drafts WHERE status = 'IGNORED' AND createdAt < :timestamp")
    suspend fun cleanupOldIgnoredDrafts(timestamp: Long)
}
