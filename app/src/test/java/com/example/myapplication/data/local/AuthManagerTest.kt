package com.example.myapplication.data.local

import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthManagerTest {

    private lateinit var storage: InMemoryAuthStorage
    private lateinit var authManager: AuthManager

    @Before
    fun setup() {
        storage = InMemoryAuthStorage()
        authManager = AuthManager(storage)
        authManager.clearTokens()
    }

    @After
    fun tearDown() {
        authManager.clearTokens()
    }

    @Test
    fun `saveTokens should store access and refresh tokens`() {
        // Given
        val accessToken = "test_access_token"
        val refreshToken = "test_refresh_token"

        // When
        authManager.saveTokens(accessToken, refreshToken)

        // Then
        assertEquals(accessToken, authManager.getAccessToken())
        assertEquals(refreshToken, authManager.getRefreshToken())
    }

    @Test
    fun `saveUserInfo should store user information`() {
        // Given
        val userId = "123"
        val email = "test@example.com"
        val displayName = "Test User"
        val isAdmin = false

        // When
        authManager.saveUserInfo(userId, email, displayName, isAdmin)

        // Then
        assertEquals(userId, authManager.getUserId())
        assertEquals(email, authManager.getUserEmail())
        assertEquals(displayName, authManager.getUserName())
        assertEquals(isAdmin, authManager.isAdmin())
    }

    @Test
    fun `saveUserInfo with admin role should set isAdmin to true`() {
        // Given
        val userId = "456"
        val email = "admin@example.com"
        val displayName = "Admin User"
        val isAdmin = true

        // When
        authManager.saveUserInfo(userId, email, displayName, isAdmin)

        // Then
        assertTrue(authManager.isAdmin())
    }

    @Test
    fun `isLoggedIn should return true when tokens are present`() {
        // Given
        authManager.saveTokens("access_token", "refresh_token")

        // When & Then
        assertTrue(authManager.isLoggedIn())
    }

    @Test
    fun `isLoggedIn should return false when tokens are not present`() {
        // Given - no tokens saved

        // When & Then
        assertFalse(authManager.isLoggedIn())
    }

    @Test
    fun `clearTokens should remove all data`() {
        // Given
        authManager.saveTokens("access_token", "refresh_token")
        authManager.saveUserInfo("1", "test@example.com", "Test", false)

        // When
        authManager.clearTokens()

        // Then
        assertNull(authManager.getAccessToken())
        assertNull(authManager.getRefreshToken())
        assertNull(authManager.getUserEmail())
        assertNull(authManager.getUserName())
        assertFalse(authManager.isLoggedIn())
    }

    @Test
    fun `getUserId should return null when not set`() {
        // Given - no user info saved

        // When & Then
        assertNull(authManager.getUserId())
    }

    @Test
    fun `tokens should persist across AuthManager instances`() {
        // Given
        val accessToken = "persistent_access_token"
        val refreshToken = "persistent_refresh_token"
        authManager.saveTokens(accessToken, refreshToken)

        // When - create new instance
        val newAuthManager = AuthManager(storage)

        // Then
        assertEquals(accessToken, newAuthManager.getAccessToken())
        assertEquals(refreshToken, newAuthManager.getRefreshToken())
    }
}
