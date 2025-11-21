package com.example.myapplication

import com.example.myapplication.data.remote.dto.TokenResponse
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

/**
 * Test suite for Token Response parsing and validation
 */
class TokenResponseTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `valid token response should parse correctly`() {
        val jsonString = """
            {
                "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                "token_type": "bearer"
            }
        """.trimIndent()

        val response = json.decodeFromString<TokenResponse>(jsonString)

        assertTrue(response.isValid())
        assertNotNull(response.accessToken)
        assertNotNull(response.refreshToken)
        assertEquals("bearer", response.tokenType)
    }

    @Test
    fun `token response with null tokens should be invalid`() {
        val response = TokenResponse(
            accessToken = null,
            refreshToken = null,
            tokenType = "bearer"
        )

        assertFalse(response.isValid())
    }

    @Test
    fun `token response with empty tokens should be invalid`() {
        val response = TokenResponse(
            accessToken = "",
            refreshToken = "",
            tokenType = "bearer"
        )

        assertFalse(response.isValid())
    }

    @Test
    fun `token response with blank tokens should be invalid`() {
        val response = TokenResponse(
            accessToken = "   ",
            refreshToken = "   ",
            tokenType = "bearer"
        )

        assertFalse(response.isValid())
    }

    @Test
    fun `token response with only access token should be invalid`() {
        val response = TokenResponse(
            accessToken = "valid_token",
            refreshToken = null,
            tokenType = "bearer"
        )

        assertFalse(response.isValid())
    }

    @Test
    fun `token response with only refresh token should be invalid`() {
        val response = TokenResponse(
            accessToken = null,
            refreshToken = "valid_token",
            tokenType = "bearer"
        )

        assertFalse(response.isValid())
    }

    @Test
    fun `getValidatedAccessToken should return token when present`() {
        val response = TokenResponse(
            accessToken = "valid_access_token",
            refreshToken = "valid_refresh_token",
            tokenType = "bearer"
        )

        assertEquals("valid_access_token", response.getValidatedAccessToken())
    }

    @Test(expected = IllegalStateException::class)
    fun `getValidatedAccessToken should throw when null`() {
        val response = TokenResponse(
            accessToken = null,
            refreshToken = "valid_refresh_token",
            tokenType = "bearer"
        )

        response.getValidatedAccessToken()
    }

    @Test
    fun `getValidatedRefreshToken should return token when present`() {
        val response = TokenResponse(
            accessToken = "valid_access_token",
            refreshToken = "valid_refresh_token",
            tokenType = "bearer"
        )

        assertEquals("valid_refresh_token", response.getValidatedRefreshToken())
    }

    @Test(expected = IllegalStateException::class)
    fun `getValidatedRefreshToken should throw when null`() {
        val response = TokenResponse(
            accessToken = "valid_access_token",
            refreshToken = null,
            tokenType = "bearer"
        )

        response.getValidatedRefreshToken()
    }

    @Test
    fun `empty JSON object should parse with null tokens`() {
        val jsonString = "{}"

        val response = json.decodeFromString<TokenResponse>(jsonString)

        assertFalse(response.isValid())
        assertNull(response.accessToken)
        assertNull(response.refreshToken)
    }

    @Test
    fun `JSON with snake_case fields should parse correctly`() {
        val jsonString = """
            {
                "access_token": "access_token_value",
                "refresh_token": "refresh_token_value",
                "token_type": "bearer"
            }
        """.trimIndent()

        val response = json.decodeFromString<TokenResponse>(jsonString)

        assertTrue(response.isValid())
        assertEquals("access_token_value", response.accessToken)
        assertEquals("refresh_token_value", response.refreshToken)
    }

    @Test
    fun `JSON with extra fields should be ignored`() {
        val jsonString = """
            {
                "access_token": "access_token_value",
                "refresh_token": "refresh_token_value",
                "token_type": "bearer",
                "user_id": 123,
                "email": "test@test.com",
                "extra_field": "ignored"
            }
        """.trimIndent()

        val response = json.decodeFromString<TokenResponse>(jsonString)

        assertTrue(response.isValid())
        assertEquals("access_token_value", response.accessToken)
        assertEquals("refresh_token_value", response.refreshToken)
    }
}

