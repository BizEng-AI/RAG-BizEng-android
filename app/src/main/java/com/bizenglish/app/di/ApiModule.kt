package com.bizenglish.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import javax.inject.Singleton
import com.bizenglish.app.data.remote.AskApi
import com.bizenglish.app.data.remote.ChatApi
import com.bizenglish.app.data.remote.RoleplayApi
import com.bizenglish.app.data.remote.PronunciationApi

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides @Singleton
    fun provideAskApi(baseUrl: String): AskApi = AskApi(baseUrl)

    @Provides @Singleton
    fun provideChatApi(baseUrl: String): ChatApi = ChatApi(baseUrl)

    @Provides @Singleton
    fun provideRoleplayApi(baseUrl: String): RoleplayApi = RoleplayApi(baseUrl)

    @Provides @Singleton
    fun providePronunciationApi(client: HttpClient, baseUrl: String): PronunciationApi =
        PronunciationApi(client, baseUrl)
}

