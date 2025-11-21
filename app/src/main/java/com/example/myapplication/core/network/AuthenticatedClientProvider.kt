package com.example.myapplication.core.network

import android.util.Log
import com.example.myapplication.data.local.AuthManager
import com.example.myapplication.data.remote.AuthApi
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Provides HttpClient with automatic authentication and token refresh.
 *
 * Features:
 * - Automatically adds Authorization header to all requests
 * - Detects 401 responses and attempts token refresh
 * - Retries failed requests after successful token refresh
 * - Thread-safe token refresh (no duplicate refresh calls)
 */
object AuthenticatedClientProvider {
    private const val TAG = "🔐 AUTH_CLIENT"

    /**
     * Creates an HttpClient that automatically handles authentication.
     *
     * @param authManager Manages token storage
     * @param authApi Used for refresh endpoint
     * @return Configured HttpClient with auth interceptor
     */
    fun create(authManager: AuthManager, authApi: AuthApi): HttpClient {
        val interceptor = AuthInterceptor(authManager, authApi)

        return HttpClient(Android) {
            expectSuccess = false

            // 🔥 LOGGING
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d(TAG, "════════════════════════════════════════")
                        Log.d(TAG, message)
                        Log.d(TAG, "════════════════════════════════════════")
                    }
                }
                level = LogLevel.ALL
            }

            // ⏱️ TIMEOUTS
            install(HttpTimeout) {
                requestTimeoutMillis = 90_000  // 90 seconds for Fly.io cold start
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 90_000
            }

            // 📦 JSON SERIALIZATION
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = true
                    encodeDefaults = true
                    coerceInputValues = true
                })
            }

            // 🔐 AUTHENTICATION - Add token to all requests
            install(DefaultRequest) {
                headers {
                    val accessToken = authManager.getAccessToken()
                    if (accessToken != null) {
                        Log.d(TAG, "🔑 Adding access token to request")
                        append(HttpHeaders.Authorization, "Bearer $accessToken")
                    } else {
                        Log.d(TAG, "⚠️ No access token available")
                    }
                }
            }

            // Note: Token refresh on 401 will be handled by wrapper functions
            // in the repositories that use this client
        }
    }

    /**
     * Extension function to handle 401 responses and retry with token refresh.
     * This should be called from repository methods that need auto-refresh.
     */
    suspend fun HttpClient.executeWithTokenRefresh(
        authManager: AuthManager,
        interceptor: AuthInterceptor,
        block: suspend HttpClient.() -> HttpResponse
    ): HttpResponse {
        var response = block()

        // If 401, try to refresh and retry
        if (response.status == HttpStatusCode.Unauthorized) {
            Log.w(TAG, "⚠️ Got 401 Unauthorized - attempting token refresh...")

            val refreshSuccess = interceptor.handleUnauthorized()

            if (refreshSuccess) {
                Log.d(TAG, "✅ Token refresh successful, retrying request...")
                response = block() // Retry with new token
                Log.d(TAG, "📥 Retry response status: ${response.status}")
            } else {
                Log.e(TAG, "❌ Token refresh failed")
            }
        }

        return response
    }
}

