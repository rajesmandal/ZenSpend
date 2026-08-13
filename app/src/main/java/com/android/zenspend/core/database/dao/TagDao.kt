package com.android.zenspend.core.database.dao

import androidx.room.*
import com.android.zenspend.core.database.entity.TagEntity
import com.android.zenspend.core.database.entity.TransactionTagCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionTagCrossRef(crossRef: TransactionTagCrossRef)

    @Delete
    suspend fun deleteTag(tag: TagEntity)
}
