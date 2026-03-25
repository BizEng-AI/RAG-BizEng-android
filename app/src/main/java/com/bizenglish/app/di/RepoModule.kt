package com.bizenglish.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.bizenglish.app.domain.repository.RagRepository
import com.bizenglish.app.data.repository.RagRepositoryImpl
import com.bizenglish.app.data.remote.AskApi
import com.bizenglish.app.data.remote.ChatApi
import com.bizenglish.app.data.remote.RoleplayApi
import com.bizenglish.app.data.remote.PronunciationApi

@Module
@InstallIn(SingletonComponent::class)
object RepoModule {
    @Provides @Singleton
    fun provideRagRepository(
        askApi: AskApi,
        chatApi: ChatApi,
        roleplayApi: RoleplayApi,
        pronunciationApi: PronunciationApi
    ): RagRepository = RagRepositoryImpl(askApi, chatApi, roleplayApi, pronunciationApi)
}

