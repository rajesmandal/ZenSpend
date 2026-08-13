package com.android.zenspend.core.database.migration

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.zenspend.core.database.AppDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate2To3Manual() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(TEST_DB)
        
        // Simulate v2 database manually
        val dbV2 = context.openOrCreateDatabase(TEST_DB, Context.MODE_PRIVATE, null)
        dbV2.execSQL("PRAGMA user_version = 2")
        dbV2.execSQL("CREATE TABLE IF NOT EXISTS `wallets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `type` TEXT NOT NULL, `openingBalance` INTEGER NOT NULL, `currentBalance` INTEGER NOT NULL, `currency` TEXT NOT NULL, `isActive` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)")
        dbV2.execSQL("CREATE TABLE IF NOT EXISTS `categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `icon` TEXT, `color` TEXT, `type` TEXT NOT NULL, `isActive` INTEGER NOT NULL, `isDefault` INTEGER NOT NULL)")
        dbV2.execSQL("CREATE TABLE IF NOT EXISTS `emis` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `walletId` INTEGER, `principalAmount` INTEGER NOT NULL, `emiAmount` INTEGER NOT NULL, `interestRate` REAL, `totalTenure` INTEGER NOT NULL, `remainingInstallments` INTEGER NOT NULL, `startDate` INTEGER NOT NULL, `endDate` INTEGER NOT NULL, `dueDay` INTEGER NOT NULL, `status` TEXT NOT NULL, FOREIGN KEY(`walletId`) REFERENCES `wallets`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )")
        dbV2.execSQL("CREATE TABLE IF NOT EXISTS `recurring_transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `amount` INTEGER NOT NULL, `type` TEXT NOT NULL, `recurrenceType` TEXT NOT NULL, `sourceWalletId` INTEGER, `categoryId` INTEGER, `startDate` INTEGER NOT NULL, `lastExecutedDate` INTEGER, `nextExecutionDate` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, `note` TEXT, FOREIGN KEY(`sourceWalletId`) REFERENCES `wallets`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL , FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )")
        dbV2.execSQL("CREATE TABLE IF NOT EXISTS `transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `amount` INTEGER NOT NULL, `date` INTEGER NOT NULL, `note` TEXT, `type` TEXT NOT NULL, `status` TEXT NOT NULL, `source` TEXT NOT NULL, `sourceWalletId` INTEGER, `destinationWalletId` INTEGER, `categoryId` INTEGER, `emiId` INTEGER, `originalTransactionId` INTEGER, `externalReference` TEXT, `originalMessage` TEXT, `isAutoDetected` INTEGER NOT NULL, `isUserConfirmed` INTEGER NOT NULL, `isExcludedFromReports` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, FOREIGN KEY(`sourceWalletId`) REFERENCES `wallets`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT , FOREIGN KEY(`destinationWalletId`) REFERENCES `wallets`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT , FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL , FOREIGN KEY(`emiId`) REFERENCES `emis`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )")
        dbV2.execSQL("CREATE TABLE IF NOT EXISTS `budgets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `categoryId` INTEGER, `amount` INTEGER NOT NULL, `period` TEXT NOT NULL, `startDate` INTEGER NOT NULL, `endDate` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        dbV2.execSQL("CREATE TABLE IF NOT EXISTS `tags` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `color` TEXT)")
        dbV2.execSQL("CREATE TABLE IF NOT EXISTS `transaction_tag_cross_ref` (`transactionId` INTEGER NOT NULL, `tagId` INTEGER NOT NULL, PRIMARY KEY(`transactionId`, `tagId`), FOREIGN KEY(`transactionId`) REFERENCES `transactions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`tagId`) REFERENCES `tags`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        dbV2.execSQL("CREATE TABLE IF NOT EXISTS `attachments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `transactionId` INTEGER NOT NULL, `uri` TEXT NOT NULL, `type` TEXT NOT NULL, `name` TEXT, FOREIGN KEY(`transactionId`) REFERENCES `transactions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        dbV2.execSQL("CREATE TABLE IF NOT EXISTS `credit_cards` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `walletId` INTEGER NOT NULL, `bankName` TEXT NOT NULL, `lastFourDigits` TEXT, `creditLimit` INTEGER NOT NULL, `billingCycleDate` INTEGER NOT NULL, `paymentDueDate` INTEGER NOT NULL, `active` INTEGER NOT NULL, FOREIGN KEY(`walletId`) REFERENCES `wallets`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        dbV2.execSQL("CREATE INDEX IF NOT EXISTS `index_credit_cards_walletId` ON `credit_cards` (`walletId`)")
        dbV2.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_transactions_sourceWalletId` ON `recurring_transactions` (`sourceWalletId`)")
        dbV2.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_transactions_categoryId` ON `recurring_transactions` (`categoryId`)")
        dbV2.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_sourceWalletId` ON `transactions` (`sourceWalletId`)")
        dbV2.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_destinationWalletId` ON `transactions` (`destinationWalletId`)")
        dbV2.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_categoryId` ON `transactions` (`categoryId`)")
        dbV2.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_emiId` ON `transactions` (`emiId`)")
        dbV2.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_date` ON `transactions` (`date`)")
        dbV2.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_type` ON `transactions` (`type`)")
        dbV2.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_externalReference` ON `transactions` (`externalReference`)")
        dbV2.execSQL("CREATE INDEX IF NOT EXISTS `index_budgets_categoryId` ON `budgets` (`categoryId`)")
        dbV2.execSQL("CREATE INDEX IF NOT EXISTS `index_transaction_tag_cross_ref_transactionId` ON `transaction_tag_cross_ref` (`transactionId`)")
        dbV2.execSQL("CREATE INDEX IF NOT EXISTS `index_transaction_tag_cross_ref_tagId` ON `transaction_tag_cross_ref` (`tagId`)")
        dbV2.execSQL("CREATE INDEX IF NOT EXISTS `index_attachments_transactionId` ON `attachments` (`transactionId`)")
        dbV2.execSQL("CREATE INDEX IF NOT EXISTS `index_emis_walletId` ON `emis` (`walletId`)")

        dbV2.execSQL("INSERT INTO wallets (name, type, openingBalance, currentBalance, currency, isActive, createdAt, updatedAt) VALUES ('Test Wallet', 'CASH', 1000, 1000, 'INR', 1, 123456789, 123456789)")
        dbV2.execSQL("INSERT INTO recurring_transactions (name, amount, type, recurrenceType, sourceWalletId, categoryId, startDate, lastExecutedDate, nextExecutionDate, isActive, note) VALUES ('Netflix', 64900, 'EXPENSE', 'MONTHLY', 1, NULL, 123456789, NULL, 123456789, 1, 'Subscription')")
        dbV2.execSQL("INSERT INTO transactions (title, amount, date, note, type, status, source, sourceWalletId, destinationWalletId, categoryId, emiId, originalTransactionId, externalReference, originalMessage, isAutoDetected, isUserConfirmed, isExcludedFromReports, createdAt, updatedAt) VALUES ('Lunch', 20000, 123456789, 'Test lunch', 'EXPENSE', 'COMPLETED', 'MANUAL', 1, NULL, NULL, NULL, NULL, NULL, NULL, 0, 1, 0, 123456789, 123456789)")
        dbV2.close()

        // Room will try to migrate it
        val dbV5 = Room.databaseBuilder(context, AppDatabase::class.java, TEST_DB)
            .addMigrations(
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5
            )
            .build()
            
        // Trigger migration by opening the database
        dbV5.openHelper.writableDatabase

        val cursorRecurring = dbV5.openHelper.readableDatabase.query("SELECT * FROM recurring_transactions")
        cursorRecurring.moveToFirst()
        assert(cursorRecurring.getColumnIndex("destinationWalletId") != -1)
        assert(cursorRecurring.getColumnIndex("createdAt") != -1)
        assert(cursorRecurring.getString(cursorRecurring.getColumnIndex("name")) == "Netflix")
        cursorRecurring.close()

        val cursorTx = dbV5.openHelper.readableDatabase.query("SELECT * FROM transactions")
        cursorTx.moveToFirst()
        assert(cursorTx.getColumnIndex("recurringTransactionId") != -1)
        assert(cursorTx.getColumnIndex("recurringOccurrenceKey") != -1)
        assert(cursorTx.getString(cursorTx.getColumnIndex("title")) == "Lunch")
        cursorTx.close()
        
        dbV5.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate4To5() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(TEST_DB)

        // Start with v4
        val dbV4 = helper.createDatabase(TEST_DB, 4)
        
        // Insert some data to verify it's preserved
        dbV4.execSQL("INSERT INTO wallets (name, type, openingBalance, currentBalance, currency, isActive, createdAt, updatedAt) VALUES ('Wallet 4', 'BANK', 100, 100, 'INR', 1, 1000, 1000)")
        dbV4.close()

        // Migrate to v5
        val dbV5 = helper.runMigrationsAndValidate(TEST_DB, 5, true, AppDatabase.MIGRATION_4_5)

        // Verify new table exists
        val cursor = dbV5.query("SELECT name FROM sqlite_master WHERE type='table' AND name='merchant_category_preferences'")
        assertTrue(cursor.moveToFirst())
        cursor.close()

        // Verify old data preserved
        val walletCursor = dbV5.query("SELECT * FROM wallets")
        assertTrue(walletCursor.moveToFirst())
        assertEquals("Wallet 4", walletCursor.getString(walletCursor.getColumnIndex("name")))
        walletCursor.close()

        dbV5.close()
    }
}
