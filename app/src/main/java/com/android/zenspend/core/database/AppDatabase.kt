package com.android.zenspend.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.android.zenspend.core.database.dao.ExpenseDao
import com.android.zenspend.core.database.entity.ExpenseEntity

@Database(entities = [ExpenseEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
}
