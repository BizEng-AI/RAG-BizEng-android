package com.example.myapplication.di

import android.content.Context
import com.example.myapplication.data.local.datastore.AuthDataStore
import com.example.myapplication.data.local.db.BizEngDatabase
import com.example.myapplication.data.local.db.ExerciseAttemptDao
import com.example.myapplication.data.local.db.PronunciationDao
import com.example.myapplication.data.local.db.RoleplayScenarioDao
import com.example.myapplication.data.remote.AskApi
import com.example.myapplication.data.remote.ChatApi
import com.example.myapplication.data.remote.PronunciationApi
import com.example.myapplication.data.remote.RoleplayApi
import com.example.myapplication.data.remote.TrackingApi
import com.example.myapplication.data.repository.ContentRepository
import com.example.myapplication.data.repository.RagRepositoryImpl
import com.example.myapplication.data.repository.TrackingRepository
import com.example.myapplication.domain.repository.RagRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BizEngDatabase = BizEngDatabase.create(context)

    @Provides @Singleton
    fun provideRoleplayScenarioDao(db: BizEngDatabase): RoleplayScenarioDao = db.roleplayScenarioDao()

    @Provides @Singleton
    fun providePronunciationDao(db: BizEngDatabase): PronunciationDao = db.pronunciationDao()

    @Provides @Singleton
    fun provideExerciseAttemptDao(db: BizEngDatabase): ExerciseAttemptDao = db.attemptDao()

    @Provides @Singleton
    fun provideAuthDataStore(@ApplicationContext context: Context): AuthDataStore = AuthDataStore(context)

    @Provides @Singleton
    fun provideContentRepository(
        roleplayDao: RoleplayScenarioDao,
        pronunciationDao: PronunciationDao
    ): ContentRepository = ContentRepository(roleplayDao, pronunciationDao)

    @Provides @Singleton
    fun provideTrackingRepository(
        trackingApi: TrackingApi,
        attemptDao: ExerciseAttemptDao
    ): TrackingRepository = TrackingRepository(trackingApi, attemptDao)
}
