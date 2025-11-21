package com.example.myapplication.data.repository

import com.example.myapplication.data.local.AuthManager
import com.example.myapplication.data.local.datastore.AuthDataStore
import com.example.myapplication.data.remote.AuthApi
import com.example.myapplication.data.remote.dto.LoginReq
import com.example.myapplication.data.remote.dto.ProfileDto
import com.example.myapplication.data.remote.dto.RegisterReq
import com.example.myapplication.data.remote.dto.TokenResponse
import io.mockk.*
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthRepositoryTest {
    private lateinit var authRepository: AuthRepository
    private lateinit var authApi: AuthApi
    private lateinit var authManager: AuthManager
    private lateinit var authDataStore: AuthDataStore

    @Before
    fun setup() {
        authApi = mockk()
        authManager = mockk(relaxed = true)
        authDataStore = mockk(relaxed = true)
        authRepository = AuthRepository(authApi, authManager, authDataStore)
    }

    @After
    fun tearDown() { clearAllMocks() }

    // Helper profile
    private fun profile(email: String = "test@example.com", roles: List<String> = listOf("student")) = ProfileDto(
        id = "1",  // updated: id now String
        email = email,
        displayName = "Test User",
        groupNumber = "Group A",
        roles = roles,
        createdAt = "2025-11-11T10:00:00Z"
    )

    @Test
    fun `register should save tokens and user info on success (profile fallback)`() = runTest {
        val email = "test@example.com"
        val password = "password123"
        val displayName = "Test User"
        val groupNumber = "Group A"
        val tokenResponse = TokenResponse(
            accessToken = "access_token",
            refreshToken = "refresh_token",
            tokenType = "bearer"
        )

        coEvery { authApi.register(RegisterReq(email, password, displayName, groupNumber)) } returns tokenResponse
        // user not in token response, so repository calls profile with access token
        coEvery { authApi.getProfile("access_token") } returns profile(email)
        every { authManager.saveTokens(any(), any()) } just Runs
        val userInfoSlot = slot<Boolean>()
        every { authManager.saveUserInfo(any(), any(), any(), capture(userInfoSlot)) } just Runs

        val result = authRepository.register(email, password, displayName, groupNumber)

        assertTrue(result.isSuccess)
        coVerify { authApi.register(RegisterReq(email, password, displayName, groupNumber)) }
        verify { authManager.saveTokens("access_token", "refresh_token") }
        verify { authManager.saveUserInfo("1", email, displayName, false) }
        assertFalse(userInfoSlot.captured)
    }

    @Test
    fun `register should propagate failure`() = runTest {
        coEvery { authApi.register(any()) } throws Exception("Registration failed")
        val result = authRepository.register("test@example.com", "password", "Test", null)
        assertTrue(result.isFailure)
    }

    @Test
    fun `login should save tokens and user info via profile fallback`() = runTest {
        val email = "test@example.com"
        val password = "password123"
        val tokenResponse = TokenResponse("access_token", "refresh_token", "bearer")

        coEvery { authApi.login(LoginReq(email, password)) } returns tokenResponse
        coEvery { authApi.getProfile("access_token") } returns profile(email)
        every { authManager.saveTokens(any(), any()) } just Runs
        val userInfoSlot = slot<Boolean>()
        every { authManager.saveUserInfo(any(), any(), any(), capture(userInfoSlot)) } just Runs

        val result = authRepository.login(email, password)
        assertTrue(result.isSuccess)
        coVerify { authApi.login(LoginReq(email, password)) }
        verify { authManager.saveTokens("access_token", "refresh_token") }
        verify { authManager.saveUserInfo("1", email, "Test User", false) }
        assertFalse(userInfoSlot.captured)
    }

    @Test
    fun `login should detect admin role`() = runTest {
        val tokenResponse = TokenResponse("access", "refresh", "bearer")
        val adminEmail = "admin@example.com"
        coEvery { authApi.login(LoginReq(adminEmail, "password")) } returns tokenResponse
        coEvery { authApi.getProfile("access") } returns profile(adminEmail, roles = listOf("admin", "teacher"))
        every { authManager.saveTokens(any(), any()) } just Runs
        val isAdminSlot = slot<Boolean>()
        every { authManager.saveUserInfo(any(), any(), any(), capture(isAdminSlot)) } just Runs

        val result = authRepository.login(adminEmail, "password")
        assertTrue(result.isSuccess)
        verify { authManager.saveUserInfo("1", adminEmail, "Test User", true) }
        assertTrue(isAdminSlot.captured)
    }

    @Test
    fun `logout should call API and clear tokens when refresh token present`() = runTest {
        every { authManager.getRefreshToken() } returns "refresh_token"
        coEvery { authApi.logout("refresh_token") } just Runs
        every { authManager.clearTokens() } just Runs

        val result = authRepository.logout()
        assertTrue(result.isSuccess)
        coVerify { authApi.logout("refresh_token") }
        verify { authManager.clearTokens() }
    }

    @Test
    fun `logout should clear tokens when refresh token missing`() = runTest {
        every { authManager.getRefreshToken() } returns null
        every { authManager.clearTokens() } just Runs

        val result = authRepository.logout()
        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { authApi.logout(any()) }
        verify { authManager.clearTokens() }
    }

    @Test
    fun `getProfile should succeed when token and API succeed`() = runTest {
        every { authManager.getAccessToken() } returns "access_token"
        coEvery { authApi.getProfile("access_token") } returns profile()

        val result = authRepository.getProfile()
        assertTrue(result.isSuccess)
        coVerify { authApi.getProfile("access_token") }
    }

    @Test
    fun `getProfile should fail when no access token`() = runTest {
        every { authManager.getAccessToken() } returns null
        val result = authRepository.getProfile()
        assertTrue(result.isFailure)
    }

    @Test
    fun `isLoggedIn delegates to AuthManager`() {
        every { authManager.isLoggedIn() } returns true
        assertTrue(authRepository.isLoggedIn())
        verify { authManager.isLoggedIn() }
    }

    @Test
    fun `isAdmin delegates to AuthManager`() {
        every { authManager.isAdmin() } returns true
        assertTrue(authRepository.isAdmin())
        verify { authManager.isAdmin() }
    }
}
