package com.example.myapplication.core.network

import android.util.Log
import com.example.myapplication.data.local.AuthManager
import com.example.myapplication.data.remote.AuthApi
import com.example.myapplication.data.remote.dto.RefreshReq
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Handles automatic token refresh when access token expires.
 *
 * This interceptor:
 * 1. Detects 401 Unauthorized responses
 * 2. Automatically tries to refresh the access token using the refresh token
 * 3. Retries the original request with the new token
 * 4. Logs out the user if refresh fails
 *
 * Thread-safe: Uses mutex to prevent multiple simultaneous refresh attempts.
 */
class AuthInterceptor(
    private val authManager: AuthManager,
    private val authApi: AuthApi
) {
    private val refreshMutex = Mutex()
    // Prevent redundant consecutive refresh calls when many requests hit 401 at once
    @Volatile private var lastSuccessfulRefreshAt: Long = 0L
    private val recentWindowMs = 1_000L

    companion object {
        private const val TAG = "🔐 AuthInterceptor"
    }

    /**
     * Handles token refresh when a 401 Unauthorized response is received.
     * Uses mutex to ensure only one refresh happens at a time if multiple
     * requests fail simultaneously.
     *
     * @return true if refresh succeeded, false if it failed (user should be logged out)
     */
    suspend fun handleUnauthorized(): Boolean {
        Log.d(TAG, "🔄 Access token expired, attempting refresh...")

        return refreshMutex.withLock {
            // If we just refreshed moments ago, skip another refresh and report success
            val now = System.currentTimeMillis()
            if (now - lastSuccessfulRefreshAt < recentWindowMs) {
                Log.d(TAG, "⏭️ Skipping redundant refresh (recent successful refresh)")
                return@withLock true
            }

            try {
                val refreshToken = authManager.getRefreshToken()

                if (refreshToken == null) {
                    Log.e(TAG, "❌ No refresh token available, cannot refresh")
                    authManager.clearTokens()
                    return@withLock false
                }

                Log.d(TAG, "📤 Calling /auth/refresh endpoint...")

                // Call refresh endpoint
                val response = authApi.refresh(refreshToken)

                // Validate response
                if (!response.isValid()) {
                    Log.e(TAG, "❌ Refresh returned invalid tokens")
                    authManager.clearTokens()
                    return@withLock false
                }

                // Save new tokens
                authManager.saveTokens(
                    response.getValidatedAccessToken(),
                    response.getValidatedRefreshToken()
                )
                lastSuccessfulRefreshAt = System.currentTimeMillis()

                Log.d(TAG, "✅ Token refresh successful!")
                Log.d(TAG, "✅ New access token: ${response.accessToken?.take(20)}...")
                Log.d(TAG, "✅ New refresh token: ${response.refreshToken?.take(20)}...")

                return@withLock true

            } catch (e: Exception) {
                Log.e(TAG, "❌ Token refresh failed: ${e.message}", e)
                Log.e(TAG, "❌ Clearing all tokens and logging out user")

                // Refresh failed, clear tokens (user will be logged out)
                authManager.clearTokens()
                return@withLock false
            }
        }
    }
}
