package com.android.zenspend.di

import com.android.zenspend.core.common.dispatcher.DefaultDispatcherProvider
import com.android.zenspend.core.common.dispatcher.DispatcherProvider
import com.android.zenspend.core.navigation.DefaultNavigator
import com.android.zenspend.core.navigation.Navigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()

    @Provides
    @Singleton
    fun provideNavigator(): Navigator = DefaultNavigator()
}
