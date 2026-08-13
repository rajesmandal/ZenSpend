package com.android.zenspend.feature.ai.di

import com.android.zenspend.BuildConfig
import com.android.zenspend.feature.ai.data.provider.GeminiAiProvider
import com.android.zenspend.feature.ai.data.provider.RemoteAiProvider
import com.android.zenspend.feature.ai.domain.repository.AiProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideAiProvider(
        geminiAiProvider: GeminiAiProvider,
        remoteAiProvider: RemoteAiProvider
    ): AiProvider {
        return if (BuildConfig.DEBUG) {
            geminiAiProvider
        } else {
            remoteAiProvider
        }
    }
}
