package com.android.zenspend.core.database.dao

import androidx.room.*
import com.android.zenspend.core.database.entity.AttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {
    @Query("SELECT * FROM attachments WHERE transactionId = :transactionId")
    fun getAttachmentsForTransaction(transactionId: Long): Flow<List<AttachmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: AttachmentEntity): Long

    @Delete
    suspend fun deleteAttachment(attachment: AttachmentEntity)
}
