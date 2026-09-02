package com.android.zenspend.di

import com.android.zenspend.core.sync.data.SyncEngine
import com.android.zenspend.core.sync.data.SyncRepositoryImpl
import com.android.zenspend.core.sync.data.SyncStatusManagerImpl
import com.android.zenspend.core.sync.domain.SyncScheduler
import com.android.zenspend.core.sync.domain.SyncStatusManager
import com.android.zenspend.core.sync.domain.repository.SyncRepository
import com.android.zenspend.feature.household.data.repository.HouseholdRepositoryImpl
import com.android.zenspend.feature.household.domain.repository.HouseholdRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    @Singleton
    abstract fun bindSyncRepository(impl: SyncRepositoryImpl): SyncRepository

    @Binds
    @Singleton
    abstract fun bindSyncScheduler(engine: SyncEngine): SyncScheduler

    @Binds
    @Singleton
    abstract fun bindSyncStatusManager(impl: SyncStatusManagerImpl): SyncStatusManager

    @Binds
    @Singleton
    abstract fun bindHouseholdRepository(impl: HouseholdRepositoryImpl): HouseholdRepository
}
