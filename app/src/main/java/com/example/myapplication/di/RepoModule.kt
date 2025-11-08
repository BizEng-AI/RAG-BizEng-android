package com.example.myapplication.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.myapplication.domain.repository.RagRepository
import com.example.myapplication.data.repository.RagRepositoryImpl
import com.example.myapplication.data.remote.AskApi
import com.example.myapplication.data.remote.ChatApi
import com.example.myapplication.data.remote.RoleplayApi
import com.example.myapplication.data.remote.PronunciationApi

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

