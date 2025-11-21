package com.example.myapplication.ui.auth

import com.example.myapplication.data.repository.AuthRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import kotlinx.coroutines.delay
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var viewModel: AuthViewModel
    private lateinit var authRepository: AuthRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk(relaxed = true)
        viewModel = AuthViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `initial state should be Idle`() = runTest {
        // When
        val state = viewModel.uiState.first()

        // Then
        assertTrue(state is AuthUiState.Idle)
    }

    @Test
    fun `login success should update state to Success and call onSuccess`() = runTest {
        // Given
        var onSuccessCalled = false
        coEvery { authRepository.login(any(), any()) } returns Result.success(Unit)

        // When
        viewModel.login("test@example.com", "password123") {
            onSuccessCalled = true
        }
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(onSuccessCalled)
        assertTrue(viewModel.uiState.value is AuthUiState.Success)
        coVerify { authRepository.login("test@example.com", "password123") }
    }

    @Test
    fun `login failure should update state to Error`() = runTest {
        // Given
        val errorMessage = "Invalid credentials"
        coEvery { authRepository.login(any(), any()) } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.login("test@example.com", "wrong_password") {}
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Error)
        assertEquals(errorMessage, (state as AuthUiState.Error).message)
    }

    @Test
    fun `login should set Loading state during API call`() = runTest {
        // Given
        coEvery { authRepository.login(any(), any()) } coAnswers {
            delay(100)
            Result.success(Unit)
        }

        // When
        viewModel.login("test@example.com", "password123") {}

        // Let the launched coroutine start and set Loading
        testDispatcher.scheduler.runCurrent()

        // Then - state should be Loading before completion
        assertTrue(viewModel.uiState.value is AuthUiState.Loading)

        // After completion, should be Success
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is AuthUiState.Success)
    }

    @Test
    fun `register success should update state to Success and call onSuccess`() = runTest {
        // Given
        var onSuccessCalled = false
        coEvery { authRepository.register(any(), any(), any(), any()) } returns Result.success(Unit)

        // When
        viewModel.register(
            email = "new@example.com",
            password = "password123",
            displayName = "New User",
            groupNumber = "Group A"
        ) {
            onSuccessCalled = true
        }
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(onSuccessCalled)
        assertTrue(viewModel.uiState.value is AuthUiState.Success)
        coVerify {
            authRepository.register("new@example.com", "password123", "New User", "Group A")
        }
    }

    @Test
    fun `register failure should update state to Error`() = runTest {
        // Given
        val errorMessage = "Email already exists"
        coEvery { authRepository.register(any(), any(), any(), any()) } returns
            Result.failure(Exception(errorMessage))

        // When
        viewModel.register("existing@example.com", "password", "User", null) {}
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Error)
        assertEquals(errorMessage, (state as AuthUiState.Error).message)
    }

    @Test
    fun `logout should call repository and invoke onSuccess`() = runTest {
        // Given
        var onSuccessCalled = false
        coEvery { authRepository.logout() } returns Result.success(Unit)

        // When
        viewModel.logout {
            onSuccessCalled = true
        }
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(onSuccessCalled)
        coVerify { authRepository.logout() }
    }

    @Test
    fun `isLoggedIn should delegate to repository`() {
        // Given
        every { authRepository.isLoggedIn() } returns true

        // When
        val result = viewModel.isLoggedIn()

        // Then
        assertTrue(result)
        verify { authRepository.isLoggedIn() }
    }

    @Test
    fun `isAdmin should delegate to repository`() {
        // Given
        every { authRepository.isAdmin() } returns false

        // When
        val result = viewModel.isAdmin()

        // Then
        assertFalse(result)
        verify { authRepository.isAdmin() }
    }

    @Test
    fun `getUserName should delegate to repository`() {
        // Given
        every { authRepository.getUserName() } returns "Test User"

        // When
        val result = viewModel.getUserName()

        // Then
        assertEquals("Test User", result)
        verify { authRepository.getUserName() }
    }

    @Test
    fun `resetState should change state back to Idle`() {
        // Given - set state to Error first
        coEvery { authRepository.login(any(), any()) } returns
            Result.failure(Exception("Error"))

        runTest {
            viewModel.login("test@example.com", "wrong") {}
            testDispatcher.scheduler.advanceUntilIdle()
            assertTrue(viewModel.uiState.value is AuthUiState.Error)

            // When
            viewModel.resetState()

            // Then
            assertTrue(viewModel.uiState.value is AuthUiState.Idle)
        }
    }
}
