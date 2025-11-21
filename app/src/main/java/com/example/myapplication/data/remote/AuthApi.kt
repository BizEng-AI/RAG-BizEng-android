package com.example.myapplication.data.remote

import android.util.Log
import com.example.myapplication.data.remote.auth.*
import com.example.myapplication.data.remote.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

class AuthApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    companion object {
        private const val TAG = "🔐 AuthApi"
    }

    suspend fun register(request: RegisterReq): TokenResponse {
        Log.d(TAG, "📤 Registering user: ${request.email}")
        Log.d(TAG, "📤 Target URL: $baseUrl/auth/register")

        return try {
            // Make the request
            val response = client.post("$baseUrl/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            val status = response.status.value
            val rawBody = response.bodyAsText()
            Log.d(TAG, "📥 HTTP Status: ${response.status}")

            // ALWAYS get raw body first for debugging
            Log.d(TAG, "📥 RAW SERVER RESPONSE: $rawBody")

            if (status == 409) throw EmailAlreadyExistsException()
            if (status == 401) throw InvalidCredentialsException()
            if (status == 429) throw RateLimitException()
            if (status in 500..599) throw ServerErrorException()
            if (rawBody.isBlank() || rawBody == "{}") throw EmptyResponseException()

            // Parse manually with explicit Json config
            val tokenResponse: TokenResponse = try {
                val json = Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    coerceInputValues = true
                }
                json.decodeFromString<TokenResponse>(rawBody)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to parse TokenResponse: ${e.message}")
                Log.e(TAG, "❌ Raw response was: $rawBody")
                throw Exception("Failed to parse TokenResponse: ${e.message}. Raw: $rawBody", e)
            }

            // Log parsed values
            Log.d(TAG, "✅ Successfully parsed TokenResponse")
            Log.d(TAG, "📦 Parsed values:")
            Log.d(TAG, "   - accessToken: ${if (tokenResponse.accessToken == null) "NULL" else "present (${tokenResponse.accessToken.length} chars)"}")
            Log.d(TAG, "   - refreshToken: ${if (tokenResponse.refreshToken == null) "NULL" else "present (${tokenResponse.refreshToken.length} chars)"}")
            Log.d(TAG, "   - tokenType: ${tokenResponse.tokenType}")

            // Validate
            if (tokenResponse.accessToken.isNullOrBlank()) {
                Log.e(TAG, "❌ Access token is null or blank after parsing!")
                Log.e(TAG, "❌ This should not happen - check serialization names")
                Log.e(TAG, "❌ Raw response: $rawBody")
                throw Exception("Server returned null or blank access token. Raw: $rawBody")
            }

            if (tokenResponse.refreshToken.isNullOrBlank()) {
                Log.e(TAG, "❌ Refresh token is null or blank after parsing!")
                Log.e(TAG, "❌ This should not happen - check serialization names")
                Log.e(TAG, "❌ Raw response: $rawBody")
                throw Exception("Server returned null or blank refresh token. Raw: $rawBody")
            }

            Log.d(TAG, "✅ Token validation passed")
            tokenResponse

        } catch (e: Exception) {
            Log.e(TAG, "❌ Register failed: ${e.message}", e)
            throw e
        }
    }

    suspend fun login(request: LoginReq): TokenResponse {
        Log.d(TAG, "📤 Logging in user: ${request.email}")
        Log.d(TAG, "📤 Target URL: $baseUrl/auth/login")

        return try {
            val response = client.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val status = response.status.value
            val rawBody = response.bodyAsText()

            // Log raw response for debugging
            Log.d(TAG, "📥 RAW SERVER RESPONSE: $rawBody")
            Log.d(TAG, "📥 HTTP Status: ${response.status}")
            Log.d(TAG, "📥 Response Headers: ${response.headers.entries().joinToString { "${it.key}=${it.value}" }}")

            if (status == 401) throw InvalidCredentialsException()
            if (status == 429) throw RateLimitException()
            if (status in 500..599) throw ServerErrorException()
            if (rawBody.isBlank() || rawBody == "{}") throw EmptyResponseException()

            // Try to parse the response
            val tokenResponse: TokenResponse = try {
                val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }
                json.decodeFromString<TokenResponse>(rawBody)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to parse response as TokenResponse: ${e.message}")
                Log.e(TAG, "❌ Raw response was: $rawBody")
                throw Exception("Server returned unparseable response: $rawBody. Expected format: {\"access_token\": \"...\", \"refresh_token\": \"...\"}", e)
            }

            // Validate the parsed response
            val hasAccessToken = !tokenResponse.accessToken.isNullOrBlank()
            val hasRefreshToken = !tokenResponse.refreshToken.isNullOrBlank()

            Log.d(TAG, "✅ Parsed response successfully")
            Log.d(TAG, "✅ Access token present: $hasAccessToken ${if (hasAccessToken) "(${tokenResponse.accessToken?.take(20)}...)" else ""}")
            Log.d(TAG, "✅ Refresh token present: $hasRefreshToken ${if (hasRefreshToken) "(${tokenResponse.refreshToken?.take(20)}...)" else ""}")

            if (!hasAccessToken || !hasRefreshToken) {
                Log.e(TAG, "❌ Server response is missing required token fields!")
                Log.e(TAG, "❌ This is a SERVER-SIDE issue. The /auth/login endpoint must return both access_token and refresh_token")
                throw Exception("Server returned incomplete token response. Access token: $hasAccessToken, Refresh token: $hasRefreshToken. Raw response: $rawBody")
            }

            tokenResponse
        } catch (e: AuthException) {
            Log.e(TAG, "❌ Domain auth error: ${e.message}")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "❌ Login failed: ${e.message}", e)
            throw when {
                e.message?.contains("incomplete token", true) == true -> IncompleteTokenResponseException()
                e.message?.contains("parse", true) == true -> TokenParseException()
                e.message?.contains("connection", true) == true -> NetworkErrorException()
                else -> UnknownAuthException()
            }
        }
    }

    suspend fun refresh(refreshToken: String): TokenResponse {
        return client.post("$baseUrl/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshReq(refreshToken))
        }.body()
    }

    suspend fun logout(refreshToken: String) {
        client.post("$baseUrl/auth/logout") {
            contentType(ContentType.Application.Json)
            setBody(RefreshReq(refreshToken))
        }
    }

    suspend fun getProfile(accessToken: String): ProfileDto {
        Log.d(TAG, "📤 Fetching user profile from /me")
        Log.d(TAG, "📤 Target URL: $baseUrl/me")
        Log.d(TAG, "📤 Using access token: ${accessToken.take(20)}...")

        return try {
            val response = client.get("$baseUrl/me") {
                headers {
                    append("Authorization", "Bearer $accessToken")
                }
            }

            val rawBody = response.bodyAsText()
            Log.d(TAG, "📥 RAW PROFILE RESPONSE: $rawBody")
            Log.d(TAG, "📥 HTTP Status: ${response.status}")

            // Check for auth errors
            if (response.status.value == 401 || response.status.value == 403) {
                Log.e(TAG, "❌ Authentication failed!")
                Log.e(TAG, "❌ Response: $rawBody")
                throw Exception("Authentication failed: $rawBody")
            }

            // Defensive: server may return an error object like {"detail":"..."} with 200 status
            if (rawBody.contains("\"detail\"")) {
                Log.e(TAG, "❌ Server returned detail-style error for /me: $rawBody")
                throw Exception("Authentication failed (server detail): $rawBody")
            }

            // Parse with explicit error handling
            Log.d(TAG, "🔍 Attempting to parse as ProfileDto...")
            try {
                val json = Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    coerceInputValues = true
                }
                val profile = json.decodeFromString<ProfileDto>(rawBody)
                Log.d(TAG, "✅ Successfully parsed ProfileDto")
                Log.d(TAG, "   - ID: ${profile.id}")
                Log.d(TAG, "   - Email: ${profile.email}")
                Log.d(TAG, "   - Display Name: ${profile.displayName}")
                Log.d(TAG, "   - Roles: ${profile.roles}")
                profile
            } catch (e: kotlinx.serialization.SerializationException) {
                Log.e(TAG, "❌ SERIALIZATION ERROR parsing ProfileDto:")
                Log.e(TAG, "❌ Error message: ${e.message}")
                Log.e(TAG, "❌ Error type: ${e.javaClass.simpleName}")
                Log.e(TAG, "❌ Stack trace:")
                e.printStackTrace()
                Log.e(TAG, "❌ Raw response that failed: $rawBody")
                throw Exception("Failed to parse ProfileDto: ${e.message}\nResponse was: $rawBody", e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ getProfile failed: ${e.message}", e)
            throw e
        }
    }
}
