package com.example.myapplication.integration

import com.example.myapplication.data.local.AuthManager
import com.example.myapplication.data.local.InMemoryAuthStorage
import com.example.myapplication.data.local.datastore.AuthDataStore
import com.example.myapplication.data.remote.AuthApi
import com.example.myapplication.data.remote.dto.*
import com.example.myapplication.data.repository.AuthRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration test for the complete authentication flow
 * Tests the interaction between AuthRepository, AuthManager, and AuthApi
 */
class AuthenticationIntegrationTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var authManager: AuthManager
    private lateinit var authApi: AuthApi
    private lateinit var storage: InMemoryAuthStorage
    private lateinit var authDataStore: AuthDataStore

    @Before
    fun setup() {
        storage = InMemoryAuthStorage()
        authManager = AuthManager(storage)
        authManager.clearTokens()

        authApi = mockk()
        authDataStore = mockk(relaxed = true)
        authRepository = AuthRepository(authApi, authManager, authDataStore)
    }

    @After
    fun tearDown() {
        authManager.clearTokens()
        clearAllMocks()
    }

    @Test
    fun `complete registration flow should store tokens and user info`() = runTest {
        // Given - Setup mock responses
        val email = "newuser@example.com"
        val password = "password123"
        val displayName = "New User"
        val groupNumber = "Group A"

        val tokenResponse = TokenResponse(
            accessToken = "test_access_token_123",
            refreshToken = "test_refresh_token_456",
            tokenType = "bearer"
        )

        val profileDto = ProfileDto(
            id = "1",
            email = email,
            displayName = displayName,
            groupNumber = groupNumber,
            roles = listOf("student"),
            createdAt = "2025-11-11T10:00:00Z"
        )

        coEvery { authApi.register(any()) } returns tokenResponse
        coEvery { authApi.getProfile("test_access_token_123") } returns profileDto

        // When - Execute registration
        val result = authRepository.register(email, password, displayName, groupNumber)

        // Then - Verify success
        assertTrue(result.isSuccess, "Registration should succeed")

        // Verify tokens are stored
        assertEquals("test_access_token_123", authManager.getAccessToken())
        assertEquals("test_refresh_token_456", authManager.getRefreshToken())

        // Verify user info is stored
        assertEquals("1", authManager.getUserId())
        assertEquals(email, authManager.getUserEmail())
        assertEquals(displayName, authManager.getUserName())
        assertEquals(false, authManager.isAdmin())

        // Verify user is logged in
        assertTrue(authManager.isLoggedIn())
    }

    @Test
    fun `complete login flow should store tokens and user info`() = runTest {
        // Given
        val email = "existing@example.com"
        val password = "password123"

        val tokenResponse = TokenResponse(
            accessToken = "login_access_token",
            refreshToken = "login_refresh_token",
            tokenType = "bearer"
        )

        val profileDto = ProfileDto(
            id = "2",
            email = email,
            displayName = "Existing User",
            groupNumber = "Group B",
            roles = listOf("student"),
            createdAt = "2025-11-10T10:00:00Z"
        )

        coEvery { authApi.login(any()) } returns tokenResponse
        coEvery { authApi.getProfile("login_access_token") } returns profileDto

        // When
        val result = authRepository.login(email, password)

        // Then
        assertTrue(result.isSuccess)
        assertNotNull(authManager.getAccessToken())
        assertNotNull(authManager.getRefreshToken())
        assertEquals(email, authManager.getUserEmail())
        assertTrue(authManager.isLoggedIn())
    }

    @Test
    fun `complete logout flow should clear all stored data`() = runTest {
        // Given - User is logged in
        authManager.saveTokens("access_token", "refresh_token")
        authManager.saveUserInfo("1", "test@example.com", "Test User", false)
        assertTrue(authManager.isLoggedIn())

        coEvery { authApi.logout(any()) } just Runs

        // When - User logs out
        val result = authRepository.logout()

        // Then - All data should be cleared
        assertTrue(result.isSuccess)
        assertEquals(null, authManager.getAccessToken())
        assertEquals(null, authManager.getRefreshToken())
        assertEquals(null, authManager.getUserEmail())
        assertEquals(null, authManager.getUserName())
        assertTrue(!authManager.isLoggedIn())
    }

    @Test
    fun `admin registration should set isAdmin flag correctly`() = runTest {
        // Given
        val tokenResponse = TokenResponse("access", "refresh", "bearer")
        val adminProfile = ProfileDto(
            id = "100",
            email = "admin@example.com",
            displayName = "Admin User",
            groupNumber = null,
            roles = listOf("admin", "teacher"),
            createdAt = "2025-11-11T10:00:00Z"
        )

        coEvery { authApi.register(any()) } returns tokenResponse
        coEvery { authApi.getProfile("access") } returns adminProfile

        // When
        val result = authRepository.register(
            "admin@example.com",
            "admin123",
            "Admin User",
            null
        )

        // Then
        assertTrue(result.isSuccess)
        assertTrue(authManager.isAdmin(), "Admin flag should be set")
        assertEquals("Admin User", authManager.getUserName())
    }

    @Test
    fun `tokens should persist across AuthManager instances`() = runTest {
        // Given - User logs in
        val tokenResponse = TokenResponse("persistent_access", "persistent_refresh", "bearer")
        val profileDto = ProfileDto(
            id = "1",
            email = "persist@example.com",
            displayName = "Persist User",
            groupNumber = null,
            roles = listOf("student"),
            createdAt = "2025-11-11T10:00:00Z"
        )

        coEvery { authApi.login(any()) } returns tokenResponse
        coEvery { authApi.getProfile("persistent_access") } returns profileDto

        authRepository.login("persist@example.com", "password")

        // When - Create new AuthManager instance (simulating app restart)
        val newAuthManager = AuthManager(storage)

        // Then - Tokens should still be available
        assertEquals("persistent_access", newAuthManager.getAccessToken())
        assertEquals("persistent_refresh", newAuthManager.getRefreshToken())
        assertTrue(newAuthManager.isLoggedIn())
    }

    @Test
    fun `failed login should not store any data`() = runTest {
        // Given - API returns error
        coEvery { authApi.login(any()) } throws Exception("Invalid credentials")

        // When
        val result = authRepository.login("wrong@example.com", "wrong_password")

        // Then
        assertTrue(result.isFailure)
        assertEquals(null, authManager.getAccessToken())
        assertEquals(null, authManager.getRefreshToken())
        assertTrue(!authManager.isLoggedIn())
    }

    @Test
    fun `getProfile should return current user data`() = runTest {
        // Given - User is logged in
        authManager.saveTokens("access_token", "refresh_token")

        val profileDto = ProfileDto(
            id = "1",
            email = "current@example.com",
            displayName = "Current User",
            groupNumber = "Group A",
            roles = listOf("student"),
            createdAt = "2025-11-11T10:00:00Z"
        )

        coEvery { authApi.getProfile("access_token") } returns profileDto

        // When
        val result = authRepository.getProfile()

        // Then
        assertTrue(result.isSuccess)
        result.getOrNull()?.let { profile ->
            assertEquals("current@example.com", profile.email)
            assertEquals("Current User", profile.displayName)
            assertEquals("Group A", profile.groupNumber)
        }
    }
}
