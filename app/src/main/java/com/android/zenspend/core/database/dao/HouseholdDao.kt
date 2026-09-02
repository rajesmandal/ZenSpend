package com.android.zenspend.core.database.dao

import androidx.room.*
import com.android.zenspend.core.database.entity.HouseholdEntity
import com.android.zenspend.core.database.entity.HouseholdMemberEntity
import com.android.zenspend.core.database.entity.SyncCursorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HouseholdDao {
    @Query("SELECT * FROM household_cache WHERE id = :id")
    suspend fun getHouseholdById(id: String): HouseholdEntity?

    @Query("SELECT * FROM household_cache LIMIT 1")
    fun observeActiveHousehold(): Flow<HouseholdEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHousehold(household: HouseholdEntity)

    @Query("DELETE FROM household_cache")
    suspend fun clearHouseholds()

    @Query("SELECT * FROM household_members_cache WHERE householdId = :householdId AND status = 'ACTIVE'")
    fun observeMembers(householdId: String): Flow<List<HouseholdMemberEntity>>

    @Query("SELECT * FROM household_members_cache WHERE householdId = :householdId AND status = 'ACTIVE'")
    suspend fun getMembers(householdId: String): List<HouseholdMemberEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMembers(members: List<HouseholdMemberEntity>)

    @Query("DELETE FROM household_members_cache WHERE householdId = :householdId")
    suspend fun clearMembers(householdId: String)

    @Query("DELETE FROM household_members_cache")
    suspend fun clearAllMembers()
}

@Dao
interface SyncCursorDao {
    @Query("SELECT * FROM sync_cursors WHERE householdId = :householdId AND entityType = :entityType")
    suspend fun getCursor(householdId: String, entityType: String): SyncCursorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCursor(cursor: SyncCursorEntity)

    @Query("DELETE FROM sync_cursors WHERE householdId = :householdId")
    suspend fun clearCursors(householdId: String)
}
