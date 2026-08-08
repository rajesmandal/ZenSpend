package com.android.zenspend.di

import com.android.zenspend.data.ai.GeminiManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGeminiManager(): GeminiManager {
        return GeminiManager()
    }
}
