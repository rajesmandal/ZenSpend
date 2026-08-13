package com.android.zenspend.di

import android.content.Context
import androidx.room.Room
import com.android.zenspend.core.database.AppDatabase
import com.android.zenspend.core.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "zenspend_db"
        )
        .addMigrations(
            AppDatabase.MIGRATION_1_2, 
            AppDatabase.MIGRATION_2_3,
            AppDatabase.MIGRATION_3_4,
            AppDatabase.MIGRATION_4_5
        )
        .build()
    }

    @Provides
    @Singleton
    fun provideWalletDao(database: AppDatabase): WalletDao = database.walletDao()

    @Provides
    @Singleton
    fun provideCategoryDao(database: AppDatabase): CategoryDao = database.categoryDao()

    @Provides
    @Singleton
    fun provideTransactionDao(database: AppDatabase): TransactionDao = database.transactionDao()

    @Provides
    @Singleton
    fun provideCreditCardDao(database: AppDatabase): CreditCardDao = database.creditCardDao()

    @Provides
    @Singleton
    fun provideEMIDao(database: AppDatabase): EMIDao = database.emiDao()

    @Provides
    @Singleton
    fun provideRecurringTransactionDao(database: AppDatabase): RecurringTransactionDao = 
        database.recurringTransactionDao()

    @Provides
    @Singleton
    fun provideBudgetDao(database: AppDatabase): BudgetDao = database.budgetDao()

    @Provides
    @Singleton
    fun provideTagDao(database: AppDatabase): TagDao = database.tagDao()

    @Provides
    @Singleton
    fun provideAttachmentDao(database: AppDatabase): AttachmentDao = database.attachmentDao()

    @Provides
    @Singleton
    fun provideSmsDraftDao(database: AppDatabase): com.android.zenspend.feature.sms.data.dao.SmsDraftDao = 
        database.smsDraftDao()

    @Provides
    @Singleton
    fun provideMerchantPreferenceDao(database: AppDatabase): com.android.zenspend.feature.category.data.dao.MerchantCategoryPreferenceDao = 
        database.merchantPreferenceDao()
}
