package com.android.zenspend.di

import com.android.zenspend.feature.ai.data.repository.AiAssistantRepositoryImpl
import com.android.zenspend.feature.ai.domain.repository.AiAssistantRepository
import com.android.zenspend.feature.ai.domain.repository.AiProvider
import com.android.zenspend.feature.ai.data.provider.GeminiAiProvider
import com.android.zenspend.feature.ai.data.provider.RemoteAiProvider
import com.android.zenspend.feature.backup.data.repository.BackupRepositoryImpl
import com.android.zenspend.feature.backup.domain.repository.BackupRepository
import com.android.zenspend.feature.sms.data.repository.SmsDraftRepositoryImpl
import com.android.zenspend.feature.sms.domain.repository.SmsDraftRepository
import com.android.zenspend.feature.sms.data.repository.SystemSmsRepositoryImpl
import com.android.zenspend.feature.sms.domain.repository.SystemSmsRepository
import com.android.zenspend.feature.budget.data.repository.BudgetRepositoryImpl
import com.android.zenspend.feature.budget.domain.repository.BudgetRepository
import com.android.zenspend.feature.category.data.repository.CategoryRepositoryImpl
import com.android.zenspend.feature.category.domain.repository.CategoryRepository
import com.android.zenspend.feature.creditcard.data.repository.CreditCardRepositoryImpl
import com.android.zenspend.feature.creditcard.domain.repository.CreditCardRepository
import com.android.zenspend.feature.emi.data.repository.EMIRepositoryImpl
import com.android.zenspend.feature.emi.domain.repository.EMIRepository
import com.android.zenspend.feature.recurring.data.repository.RecurringTransactionRepositoryImpl
import com.android.zenspend.feature.recurring.domain.repository.RecurringTransactionRepository
import com.android.zenspend.feature.report.data.repository.ReportRepositoryImpl
import com.android.zenspend.feature.report.domain.repository.ReportRepository
import com.android.zenspend.feature.transaction.data.repository.TransactionRepositoryImpl
import com.android.zenspend.feature.transaction.domain.repository.TransactionRepository
import com.android.zenspend.feature.wallet.data.repository.WalletRepositoryImpl
import com.android.zenspend.feature.wallet.domain.repository.WalletRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAiAssistantRepository(
        aiAssistantRepositoryImpl: AiAssistantRepositoryImpl
    ): AiAssistantRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        transactionRepositoryImpl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindWalletRepository(
        walletRepositoryImpl: WalletRepositoryImpl
    ): WalletRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindCreditCardRepository(
        creditCardRepositoryImpl: CreditCardRepositoryImpl
    ): CreditCardRepository

    @Binds
    @Singleton
    abstract fun bindEMIRepository(
        emiRepositoryImpl: EMIRepositoryImpl
    ): EMIRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(
        budgetRepositoryImpl: BudgetRepositoryImpl
    ): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindReportRepository(
        reportRepositoryImpl: ReportRepositoryImpl
    ): ReportRepository

    @Binds
    @Singleton
    abstract fun bindRecurringTransactionRepository(
        recurringTransactionRepositoryImpl: RecurringTransactionRepositoryImpl
    ): RecurringTransactionRepository

    @Binds
    @Singleton
    abstract fun bindBackupRepository(
        backupRepositoryImpl: BackupRepositoryImpl
    ): BackupRepository

    @Binds
    @Singleton
    abstract fun bindSmsDraftRepository(
        smsDraftRepositoryImpl: SmsDraftRepositoryImpl
    ): SmsDraftRepository

    @Binds
    @Singleton
    abstract fun bindSystemSmsRepository(
        systemSmsRepositoryImpl: SystemSmsRepositoryImpl
    ): SystemSmsRepository
}
