package com.example.myapplication.data.repository

import com.example.myapplication.core.network.AuthInterceptor
import com.example.myapplication.data.remote.AdminApi
import com.example.myapplication.data.remote.dto.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FakeRetryHandler : UnauthorizedRetryHandler {
    override suspend fun <T> runWithRefresh(block: suspend () -> T): T = block()
}

@OptIn(ExperimentalCoroutinesApi::class)
class AdminRepositoryTest {

    private val adminApi: AdminApi = mockk()
    private val retryHandler: UnauthorizedRetryHandler = FakeRetryHandler()
    private val repository = AdminRepository(adminApi, retryHandler)

    @Test
    fun `getOverview returns success`() = runTest {
        coEvery { adminApi.getOverview() } returns AdminOverviewDto(totals = OverviewTotalsDto(1, 2))
        val result = repository.getOverview()
        assertTrue(result.isSuccess)
        coVerify { adminApi.getOverview() }
    }

    @Test
    fun `getActiveToday returns success`() = runTest {
        coEvery { adminApi.getActiveToday() } returns ActiveTodayDto("2025-11-15", 5)
        val result = repository.getActiveToday()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `getAttemptsDaily returns success`() = runTest {
        coEvery { adminApi.getAttemptsDaily() } returns listOf(DayCountDto("2025-11-15", 5))
        val result = repository.getAttemptsDaily()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `getUsersSignupsDaily returns success`() = runTest {
        coEvery { adminApi.getUsersSignupsDaily() } returns listOf(DayCountDto("2025-11-10", 3))
        val result = repository.getUsersSignupsDaily()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `getRecentAttempts returns success`() = runTest {
        coEvery { adminApi.getRecentAttempts(any()) } returns listOf(
            RecentAttemptDto(1, "email", "name", "chat", "id", 0.8f, 100, "start", "end")
        )
        val result = repository.getRecentAttempts()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `getOverview returns failure on API exception`() = runTest {
        coEvery { adminApi.getOverview() } throws RuntimeException("boom")
        val result = repository.getOverview()
        assertTrue(result.isFailure)
    }
}
