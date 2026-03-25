package com.bizenglish.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import io.ktor.client.*
import com.bizenglish.app.BuildConfig
import com.bizenglish.app.core.network.KtorClientProvider

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun provideBaseUrl(): String = BuildConfig.BASE_URL

    @Provides @Singleton
    fun provideHttpClient(): HttpClient = KtorClientProvider.client
}
