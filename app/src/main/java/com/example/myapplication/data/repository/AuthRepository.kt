package com.example.myapplication.data.repository

import com.example.myapplication.data.local.AuthManager
import com.example.myapplication.data.local.datastore.AuthDataStore
import com.example.myapplication.data.remote.AuthApi
import com.example.myapplication.data.remote.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val authManager: AuthManager,
    private val authDataStore: AuthDataStore
) {

    suspend fun register(
        email: String,
        password: String,
        displayName: String,
        groupNumber: String?
    ): Result<Unit> = runCatching {
        android.util.Log.d("AuthRepository", "🔵 START register()")
        android.util.Log.d("AuthRepository", "   Email: $email")
        android.util.Log.d("AuthRepository", "   Display Name: $displayName")

        android.util.Log.d("AuthRepository", "🔵 STEP 1: Calling authApi.register()...")
        val response = authApi.register(
            RegisterReq(email, password, displayName, groupNumber)
        )

        // Validate response has required fields
        android.util.Log.d("AuthRepository", "🔵 STEP 2: Validating token response...")
        if (!response.isValid()) {
            throw IllegalStateException("Server returned invalid token response. Access token: ${response.accessToken != null}, Refresh token: ${response.refreshToken != null}")
        }
        android.util.Log.d("AuthRepository", "   ✅ Token response is valid")

        android.util.Log.d("AuthRepository", "🔵 STEP 3: Saving tokens to AuthManager...")
        authManager.saveTokens(
            response.getValidatedAccessToken(),
            response.getValidatedRefreshToken()
        )
        android.util.Log.d("AuthRepository", "   ✅ Tokens saved successfully")

        // Save user info from response (if available) or fetch from server
        android.util.Log.d("AuthRepository", "🔵 STEP 4: Getting user info...")
        if (response.user != null) {
            android.util.Log.d("AuthRepository", "   ℹ️  User data found in token response")
            val isAdmin = response.user.roles.any { it.equals("admin", ignoreCase = true) }
            authManager.saveUserInfo(
                response.user.id,
                response.user.email,
                response.user.displayName,
                isAdmin
            )
            android.util.Log.d("AuthRepository", "   ✅ User info saved from token response")
        } else {
            android.util.Log.d("AuthRepository", "   ℹ️  No user data in token response, fetching from /me...")
            // Fallback: Fetch profile if user not in response
            try {
                android.util.Log.d("AuthRepository", "   🔵 Calling authApi.getProfile() with access token...")
                val accessToken = response.getValidatedAccessToken()
                val profile = authApi.getProfile(accessToken)
                android.util.Log.d("AuthRepository", "   ✅ Profile fetched successfully")

                val isAdmin = profile.roles.any { it.equals("admin", ignoreCase = true) }
                authManager.saveUserInfo(
                    profile.id,
                    profile.email,
                    profile.displayName,
                    isAdmin
                )
                android.util.Log.d("AuthRepository", "   ✅ User info saved from profile")
            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "   ❌ Failed to fetch profile: ${e.message}", e)
                throw e
            }
        }

        android.util.Log.d("AuthRepository", "🔵 ✅ REGISTRATION COMPLETE")
        Unit
    }.onSuccess {
        // Persist to DataStore as well
        withContext(Dispatchers.IO) {
            authDataStore.saveAuth(
                token = authManager.getAccessToken() ?: "",
                email = authManager.getUserEmail() ?: email,
                name = authManager.getUserName()
            )
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        val response = authApi.login(LoginReq(email, password))

        // Validate response has required fields
        if (!response.isValid()) {
            throw IllegalStateException("Server returned invalid token response. Access token: ${response.accessToken != null}, Refresh token: ${response.refreshToken != null}")
        }

        authManager.saveTokens(
            response.getValidatedAccessToken(),
            response.getValidatedRefreshToken()
        )

        // Save user info from response (if available) or fetch from server
        if (response.user != null) {
            val isAdmin = response.user.roles.any { it.equals("admin", ignoreCase = true) }
            authManager.saveUserInfo(
                response.user.id,
                response.user.email,
                response.user.displayName,
                isAdmin
            )
        } else {
            // Fallback: Fetch profile if user not in response
            val accessToken = response.getValidatedAccessToken()
            val profile = authApi.getProfile(accessToken)
            val isAdmin = profile.roles.any { it.equals("admin", ignoreCase = true) }
            authManager.saveUserInfo(
                profile.id,
                profile.email,
                profile.displayName,
                isAdmin
            )
        }
        Unit
    }.onSuccess {
        withContext(Dispatchers.IO) {
            authDataStore.saveAuth(
                token = authManager.getAccessToken() ?: "",
                email = authManager.getUserEmail() ?: email,
                name = authManager.getUserName()
            )
        }
    }

    suspend fun logout(): Result<Unit> = runCatching {
        val refreshToken = authManager.getRefreshToken()
        if (refreshToken != null) {
            authApi.logout(refreshToken)
        }
        authManager.clearTokens()
    }.onSuccess {
        withContext(Dispatchers.IO) { authDataStore.clearAuth() }
    }

    suspend fun refresh(refreshToken: String): Result<Unit> = runCatching {
        val response = authApi.refresh(refreshToken)

        // Validate response has required fields
        if (!response.isValid()) {
            throw IllegalStateException("Server returned invalid token response during refresh")
        }

        authManager.saveTokens(
            response.getValidatedAccessToken(),
            response.getValidatedRefreshToken()
        )
    }

    suspend fun getProfile(): Result<ProfileDto> = runCatching {
        val accessToken = authManager.getAccessToken()
            ?: throw IllegalStateException("No access token available")
        authApi.getProfile(accessToken)
    }

    fun isLoggedIn(): Boolean = authManager.isLoggedIn()

    fun isAdmin(): Boolean = authManager.isAdmin()

    fun getUserName(): String? = authManager.getUserName()

    fun getUserEmail(): String? = authManager.getUserEmail()
}
