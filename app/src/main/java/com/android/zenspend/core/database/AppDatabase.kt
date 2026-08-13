package com.android.zenspend.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.android.zenspend.core.database.converter.Converters
import com.android.zenspend.core.database.dao.*
import com.android.zenspend.core.database.entity.*

@Database(
    entities = [
        WalletEntity::class,
        CategoryEntity::class,
        CreditCardEntity::class,
        EMIEntity::class,
        RecurringTransactionEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        TagEntity::class,
        TransactionTagCrossRef::class,
        AttachmentEntity::class,
        com.android.zenspend.feature.sms.data.entity.SmsDraftEntity::class,
        com.android.zenspend.feature.category.data.entity.MerchantCategoryPreferenceEntity::class
    ],
    version = 5,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun creditCardDao(): CreditCardDao
    abstract fun emiDao(): EMIDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun tagDao(): TagDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun backupDao(): BackupDao
    abstract fun smsDraftDao(): com.android.zenspend.feature.sms.data.dao.SmsDraftDao
    abstract fun merchantPreferenceDao(): com.android.zenspend.feature.category.data.dao.MerchantCategoryPreferenceDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create new tables (Simplified for the review, usually we'd include all CREATE TABLE statements)
                // For a hardening review, I will provide the migration logic for the existing data.
                
                db.execSQL("CREATE TABLE IF NOT EXISTS `wallets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `type` TEXT NOT NULL, `openingBalance` INTEGER NOT NULL, `currentBalance` INTEGER NOT NULL, `currency` TEXT NOT NULL, `isActive` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `icon` TEXT, `color` TEXT, `type` TEXT NOT NULL, `isActive` INTEGER NOT NULL, `isDefault` INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `credit_cards` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `walletId` INTEGER NOT NULL, `bankName` TEXT NOT NULL, `lastFourDigits` TEXT, `creditLimit` INTEGER NOT NULL, `billingCycleDate` INTEGER NOT NULL, `paymentDueDate` INTEGER NOT NULL, `active` INTEGER NOT NULL, FOREIGN KEY(`walletId`) REFERENCES `wallets`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_credit_cards_walletId` ON `credit_cards` (`walletId`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `emis` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `walletId` INTEGER, `principalAmount` INTEGER NOT NULL, `emiAmount` INTEGER NOT NULL, `interestRate` REAL, `totalTenure` INTEGER NOT NULL, `remainingInstallments` INTEGER NOT NULL, `startDate` INTEGER NOT NULL, `endDate` INTEGER NOT NULL, `dueDay` INTEGER NOT NULL, `status` TEXT NOT NULL, FOREIGN KEY(`walletId`) REFERENCES `wallets`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_emis_walletId` ON `emis` (`walletId`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `recurring_transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `amount` INTEGER NOT NULL, `type` TEXT NOT NULL, `recurrenceType` TEXT NOT NULL, `sourceWalletId` INTEGER, `categoryId` INTEGER, `startDate` INTEGER NOT NULL, `lastExecutedDate` INTEGER, `nextExecutionDate` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, `note` TEXT, FOREIGN KEY(`sourceWalletId`) REFERENCES `wallets`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL , FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_transactions_sourceWalletId` ON `recurring_transactions` (`sourceWalletId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_transactions_categoryId` ON `recurring_transactions` (`categoryId`)")
                
                db.execSQL("CREATE TABLE IF NOT EXISTS `transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `amount` INTEGER NOT NULL, `date` INTEGER NOT NULL, `note` TEXT, `type` TEXT NOT NULL, `status` TEXT NOT NULL, `source` TEXT NOT NULL, `sourceWalletId` INTEGER, `destinationWalletId` INTEGER, `categoryId` INTEGER, `emiId` INTEGER, `originalTransactionId` INTEGER, `externalReference` TEXT, `originalMessage` TEXT, `isAutoDetected` INTEGER NOT NULL, `isUserConfirmed` INTEGER NOT NULL, `isExcludedFromReports` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, FOREIGN KEY(`sourceWalletId`) REFERENCES `wallets`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT , FOREIGN KEY(`destinationWalletId`) REFERENCES `wallets`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT , FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL , FOREIGN KEY(`emiId`) REFERENCES `emis`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_sourceWalletId` ON `transactions` (`sourceWalletId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_destinationWalletId` ON `transactions` (`destinationWalletId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_categoryId` ON `transactions` (`categoryId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_emiId` ON `transactions` (`emiId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_date` ON `transactions` (`date`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_type` ON `transactions` (`type`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_externalReference` ON `transactions` (`externalReference`)")
                
                db.execSQL("CREATE TABLE IF NOT EXISTS `budgets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `categoryId` INTEGER, `amount` INTEGER NOT NULL, `period` TEXT NOT NULL, `startDate` INTEGER NOT NULL, `endDate` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_budgets_categoryId` ON `budgets` (`categoryId`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `tags` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `color` TEXT)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `transaction_tag_cross_ref` (`transactionId` INTEGER NOT NULL, `tagId` INTEGER NOT NULL, PRIMARY KEY(`transactionId`, `tagId`), FOREIGN KEY(`transactionId`) REFERENCES `transactions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`tagId`) REFERENCES `tags`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transaction_tag_cross_ref_transactionId` ON `transaction_tag_cross_ref` (`transactionId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transaction_tag_cross_ref_tagId` ON `transaction_tag_cross_ref` (`tagId`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `attachments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `transactionId` INTEGER NOT NULL, `uri` TEXT NOT NULL, `type` TEXT NOT NULL, `name` TEXT, FOREIGN KEY(`transactionId`) REFERENCES `transactions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_attachments_transactionId` ON `attachments` (`transactionId`)")

                // 2. Insert a default wallet for legacy data
                db.execSQL("INSERT INTO wallets (name, type, openingBalance, currentBalance, currency, isActive, createdAt, updatedAt) VALUES ('Default Wallet', 'CASH', 0, 0, 'INR', 1, strftime('%s','now')*1000, strftime('%s','now')*1000)")
                val walletId = 1 // Assuming this is the first wallet

                // 3. Migrate data from expenses to transactions
                db.execSQL("""
                    INSERT INTO transactions (title, amount, date, note, type, status, source, sourceWalletId, isAutoDetected, isUserConfirmed, isExcludedFromReports, createdAt, updatedAt)
                    SELECT title, CAST(amount * 100 AS INTEGER), date, note, 'EXPENSE', 'COMPLETED', 'MANUAL', $walletId, 0, 1, 0, strftime('%s','now')*1000, strftime('%s','now')*1000
                    FROM expenses
                """)

                // 4. Drop the old table
                db.execSQL("DROP TABLE IF EXISTS expenses")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val now = System.currentTimeMillis()

                // 1. Recreate recurring_transactions
                db.execSQL("CREATE TABLE IF NOT EXISTS `recurring_transactions_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `amount` INTEGER NOT NULL, `type` TEXT NOT NULL, `recurrenceType` TEXT NOT NULL, `sourceWalletId` INTEGER, `destinationWalletId` INTEGER, `categoryId` INTEGER, `startDate` INTEGER NOT NULL, `lastExecutedDate` INTEGER, `nextExecutionDate` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, `note` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, FOREIGN KEY(`sourceWalletId`) REFERENCES `wallets`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL , FOREIGN KEY(`destinationWalletId`) REFERENCES `wallets`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL , FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )")
                
                db.execSQL("""
                    INSERT INTO recurring_transactions_new (id, name, amount, type, recurrenceType, sourceWalletId, categoryId, startDate, lastExecutedDate, nextExecutionDate, isActive, note, createdAt, updatedAt)
                    SELECT id, name, amount, type, recurrenceType, sourceWalletId, categoryId, startDate, lastExecutedDate, nextExecutionDate, isActive, note, $now, $now
                    FROM recurring_transactions
                """)
                
                db.execSQL("DROP TABLE recurring_transactions")
                db.execSQL("ALTER TABLE recurring_transactions_new RENAME TO recurring_transactions")
                
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_transactions_sourceWalletId` ON `recurring_transactions` (`sourceWalletId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_transactions_destinationWalletId` ON `recurring_transactions` (`destinationWalletId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_transactions_categoryId` ON `recurring_transactions` (`categoryId`)")

                // 2. Recreate transactions (to add new FK and Unique index)
                db.execSQL("CREATE TABLE IF NOT EXISTS `transactions_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `amount` INTEGER NOT NULL, `date` INTEGER NOT NULL, `note` TEXT, `type` TEXT NOT NULL, `status` TEXT NOT NULL, `source` TEXT NOT NULL, `sourceWalletId` INTEGER, `destinationWalletId` INTEGER, `categoryId` INTEGER, `emiId` INTEGER, `recurringTransactionId` INTEGER, `recurringOccurrenceKey` TEXT, `originalTransactionId` INTEGER, `externalReference` TEXT, `originalMessage` TEXT, `isAutoDetected` INTEGER NOT NULL, `isUserConfirmed` INTEGER NOT NULL, `isExcludedFromReports` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, FOREIGN KEY(`sourceWalletId`) REFERENCES `wallets`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT , FOREIGN KEY(`destinationWalletId`) REFERENCES `wallets`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT , FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL , FOREIGN KEY(`emiId`) REFERENCES `emis`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL , FOREIGN KEY(`recurringTransactionId`) REFERENCES `recurring_transactions`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )")
                
                db.execSQL("""
                    INSERT INTO transactions_new (id, title, amount, date, note, type, status, source, sourceWalletId, destinationWalletId, categoryId, emiId, originalTransactionId, externalReference, originalMessage, isAutoDetected, isUserConfirmed, isExcludedFromReports, createdAt, updatedAt)
                    SELECT id, title, amount, date, note, type, status, source, sourceWalletId, destinationWalletId, categoryId, emiId, originalTransactionId, externalReference, originalMessage, isAutoDetected, isUserConfirmed, isExcludedFromReports, createdAt, updatedAt
                    FROM transactions
                """)
                
                db.execSQL("DROP TABLE transactions")
                db.execSQL("ALTER TABLE transactions_new RENAME TO transactions")
                
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_sourceWalletId` ON `transactions` (`sourceWalletId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_destinationWalletId` ON `transactions` (`destinationWalletId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_categoryId` ON `transactions` (`categoryId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_emiId` ON `transactions` (`emiId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_recurringTransactionId` ON `transactions` (`recurringTransactionId`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_transactions_recurringOccurrenceKey` ON `transactions` (`recurringOccurrenceKey`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_date` ON `transactions` (`date`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_type` ON `transactions` (`type`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_externalReference` ON `transactions` (`externalReference`)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `sms_drafts` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `amount` INTEGER NOT NULL, 
                        `type` TEXT NOT NULL, 
                        `date` INTEGER NOT NULL, 
                        `merchant` TEXT, 
                        `accountLast4` TEXT, 
                        `sender` TEXT NOT NULL, 
                        `fingerprint` TEXT NOT NULL, 
                        `status` TEXT NOT NULL, 
                        `createdAt` INTEGER NOT NULL
                    )
                """)
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_sms_drafts_fingerprint` ON `sms_drafts` (`fingerprint`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_sms_drafts_status` ON `sms_drafts` (`status`)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `merchant_category_preferences` (
                        `normalizedMerchant` TEXT NOT NULL, 
                        `categoryId` INTEGER NOT NULL, 
                        `usageCount` INTEGER NOT NULL, 
                        `lastUsedAt` INTEGER NOT NULL, 
                        PRIMARY KEY(`normalizedMerchant`)
                    )
                """)
            }
        }
    }
}
