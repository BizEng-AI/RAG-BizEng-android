package com.example.myapplication.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import javax.inject.Singleton
import com.example.myapplication.data.remote.AskApi
import com.example.myapplication.data.remote.ChatApi
import com.example.myapplication.data.remote.RoleplayApi
import com.example.myapplication.data.remote.PronunciationApi
import com.example.myapplication.data.remote.AdminApi
import com.example.myapplication.data.repository.AdminRepository
import com.example.myapplication.core.network.AuthInterceptor
import com.example.myapplication.data.repository.AuthInterceptorRetryHandler
import com.example.myapplication.data.repository.UnauthorizedRetryHandler

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    /**
     * All these APIs use the authenticated client which automatically:
     * - Adds Authorization header
     * - Handles token refresh on 401 responses
     * - Retries failed requests after refresh
     */

    @Provides @Singleton
    fun provideAskApi(
        @javax.inject.Named("AuthenticatedClient") client: HttpClient,
        baseUrl: String
    ): AskApi = AskApi(client, baseUrl)

    @Provides @Singleton
    fun provideChatApi(
        @javax.inject.Named("AuthenticatedClient") client: HttpClient,
        baseUrl: String
    ): ChatApi = ChatApi(client, baseUrl)

    @Provides @Singleton
    fun provideRoleplayApi(
        @javax.inject.Named("AuthenticatedClient") client: HttpClient,
        baseUrl: String
    ): RoleplayApi = RoleplayApi(client, baseUrl)

    @Provides @Singleton
    fun providePronunciationApi(
        @javax.inject.Named("AuthenticatedClient") client: HttpClient,
        baseUrl: String
    ): PronunciationApi = PronunciationApi(client, baseUrl)

    @Provides @Singleton
    fun provideAdminApi(
        @javax.inject.Named("AuthenticatedClient") client: HttpClient,
        baseUrl: String
    ): AdminApi = AdminApi(client, baseUrl)

    @Provides @Singleton
    fun provideUnauthorizedRetryHandler(
        authInterceptor: AuthInterceptor
    ): UnauthorizedRetryHandler = AuthInterceptorRetryHandler(authInterceptor)

    @Provides @Singleton
    fun provideAdminRepository(
        adminApi: AdminApi,
        retryHandler: UnauthorizedRetryHandler
    ): AdminRepository = AdminRepository(adminApi, retryHandler)
}
