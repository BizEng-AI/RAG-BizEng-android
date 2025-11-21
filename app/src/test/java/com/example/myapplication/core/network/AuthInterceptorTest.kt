package com.example.myapplication.core.network

import com.example.myapplication.data.local.AuthManager
import com.example.myapplication.data.remote.AuthApi
import com.example.myapplication.data.remote.dto.TokenResponse
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.async
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for AuthInterceptor to verify token refresh logic
 */
class AuthInterceptorTest {

    private lateinit var authManager: AuthManager
    private lateinit var authApi: AuthApi
    private lateinit var interceptor: AuthInterceptor

    @Before
    fun setup() {
        authManager = mockk()
        authApi = mockk()
        interceptor = AuthInterceptor(authManager, authApi)
    }

    @Test
    fun `handleUnauthorized should refresh token successfully`() = runBlocking {
        // Given: Valid refresh token and successful API response
        val refreshToken = "valid_refresh_token"
        val newAccessToken = "new_access_token"
        val newRefreshToken = "new_refresh_token"

        val tokenResponse = spyk(
            TokenResponse(
                accessToken = newAccessToken,
                refreshToken = newRefreshToken,
                tokenType = "bearer"
            )
        )

        every { authManager.getRefreshToken() } returns refreshToken
        coEvery { authApi.refresh(refreshToken) } returns tokenResponse
        every { tokenResponse.isValid() } returns true
        every { tokenResponse.getValidatedAccessToken() } returns newAccessToken
        every { tokenResponse.getValidatedRefreshToken() } returns newRefreshToken
        every { authManager.saveTokens(newAccessToken, newRefreshToken) } just Runs

        // When: Token refresh is triggered
        val result = interceptor.handleUnauthorized()

        // Then: Should succeed and save new tokens
        assertTrue(result)
        verify { authManager.saveTokens(newAccessToken, newRefreshToken) }
        verify(exactly = 0) { authManager.clearTokens() }
    }

    @Test
    fun `handleUnauthorized should clear tokens when refresh token is null`() = runBlocking {
        // Given: No refresh token available
        every { authManager.getRefreshToken() } returns null
        every { authManager.clearTokens() } just Runs

        // When: Token refresh is triggered
        val result = interceptor.handleUnauthorized()

        // Then: Should fail and clear tokens
        assertFalse(result)
        verify { authManager.clearTokens() }
        verify(exactly = 0) { authManager.saveTokens(any(), any()) }
    }

    @Test
    fun `handleUnauthorized should clear tokens when refresh fails`() = runBlocking {
        // Given: Refresh token exists but API call fails
        val refreshToken = "valid_refresh_token"

        every { authManager.getRefreshToken() } returns refreshToken
        coEvery { authApi.refresh(refreshToken) } throws Exception("Network error")
        every { authManager.clearTokens() } just Runs

        // When: Token refresh is triggered
        val result = interceptor.handleUnauthorized()

        // Then: Should fail and clear tokens
        assertFalse(result)
        verify { authManager.clearTokens() }
        verify(exactly = 0) { authManager.saveTokens(any(), any()) }
    }

    @Test
    fun `handleUnauthorized should clear tokens when response is invalid`() = runBlocking {
        // Given: Refresh succeeds but returns invalid tokens
        val refreshToken = "valid_refresh_token"
        val tokenResponse = spyk(
            TokenResponse(
                accessToken = null,  // Invalid!
                refreshToken = "new_refresh_token",
                tokenType = "bearer"
            )
        )

        every { authManager.getRefreshToken() } returns refreshToken
        coEvery { authApi.refresh(refreshToken) } returns tokenResponse
        every { tokenResponse.isValid() } returns false
        every { authManager.clearTokens() } just Runs

        // When: Token refresh is triggered
        val result = interceptor.handleUnauthorized()

        // Then: Should fail and clear tokens
        assertFalse(result)
        verify { authManager.clearTokens() }
        verify(exactly = 0) { authManager.saveTokens(any(), any()) }
    }

    @Test
    fun `multiple concurrent refresh calls should only refresh once`() = runBlocking {
        // Given: Valid refresh token
        val refreshToken = "valid_refresh_token"
        val newAccessToken = "new_access_token"
        val newRefreshToken = "new_refresh_token"

        val tokenResponse = spyk(
            TokenResponse(
                accessToken = newAccessToken,
                refreshToken = newRefreshToken,
                tokenType = "bearer"
            )
        )

        every { authManager.getRefreshToken() } returns refreshToken
        coEvery { authApi.refresh(refreshToken) } coAnswers {
            // small delay to create overlap and allow mutex to serialize
            delay(50)
            tokenResponse
        }
        every { tokenResponse.isValid() } returns true
        every { tokenResponse.getValidatedAccessToken() } returns newAccessToken
        every { tokenResponse.getValidatedRefreshToken() } returns newRefreshToken
        every { authManager.saveTokens(newAccessToken, newRefreshToken) } just Runs

        // When: Multiple concurrent refresh calls
        val deferreds: List<Deferred<Boolean>> = (1..5).map {
            async {
                interceptor.handleUnauthorized()
            }
        }

        val results: List<Boolean> = deferreds.awaitAll()

        // Then: All should report success but API should only be called once
        assertTrue(results.all { it })
        coVerify(exactly = 1) { authApi.refresh(refreshToken) }
        verify(exactly = 1) { authManager.saveTokens(newAccessToken, newRefreshToken) }
    }
}
