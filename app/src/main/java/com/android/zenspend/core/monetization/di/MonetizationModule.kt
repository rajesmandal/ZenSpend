package com.android.zenspend.core.monetization.di

import com.android.zenspend.BuildConfig
import com.android.zenspend.core.monetization.data.repository.MockBillingRepositoryImpl
import com.android.zenspend.core.monetization.data.repository.PlayBillingRepositoryImpl
import com.android.zenspend.core.monetization.domain.repository.BillingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MonetizationModule {

    @Provides
    @Singleton
    fun provideBillingRepository(
        playBillingRepositoryImpl: PlayBillingRepositoryImpl,
        mockBillingRepositoryImpl: MockBillingRepositoryImpl
    ): BillingRepository {
        return if (BuildConfig.DEBUG) {
            mockBillingRepositoryImpl
        } else {
            playBillingRepositoryImpl
        }
    }
}
