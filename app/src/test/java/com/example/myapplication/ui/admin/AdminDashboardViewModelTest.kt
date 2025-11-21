package com.example.myapplication.ui.admin

import com.example.myapplication.data.remote.dto.ActiveTodayDto
import com.example.myapplication.data.remote.dto.AdminOverviewDto
import com.example.myapplication.data.remote.dto.DayCountDto
import com.example.myapplication.data.remote.dto.OverviewTotalsDto
import com.example.myapplication.data.remote.dto.RecentAttemptDto
import com.example.myapplication.data.repository.AdminRepository
import com.example.myapplication.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AdminDashboardViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val repository: AdminRepository = mockk()

    @Test
    fun `loadDashboard success emits Success state`() = runTest {
        coEvery { repository.getOverview() } returns Result.success(AdminOverviewDto(totals = OverviewTotalsDto(1, 2)))
        coEvery { repository.getAttemptsDaily() } returns Result.success(listOf(DayCountDto("2025-11-15", 5)))
        coEvery { repository.getUsersSignupsDaily() } returns Result.success(emptyList())
        coEvery { repository.getActiveToday() } returns Result.success(ActiveTodayDto("2025-11-15", 2))
        coEvery { repository.getRecentAttempts(any()) } returns Result.success(
            listOf(RecentAttemptDto(1, "email", "name", "chat", "id", 0.9f, 120, "start", "end"))
        )

        val viewModel = AdminDashboardViewModel(repository, dispatcherRule.testDispatcher)
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is AdminDashboardUiState.Success)
    }

    @Test
    fun `loadDashboard failure emits Error state`() = runTest {
        coEvery { repository.getOverview() } returns Result.failure(Exception("boom"))
        coEvery { repository.getAttemptsDaily() } returns Result.success(emptyList())
        coEvery { repository.getUsersSignupsDaily() } returns Result.success(emptyList())
        coEvery { repository.getActiveToday() } returns Result.success(ActiveTodayDto("2025-11-15", 0))
        coEvery { repository.getRecentAttempts(any()) } returns Result.success(emptyList())

        val viewModel = AdminDashboardViewModel(repository, dispatcherRule.testDispatcher)
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is AdminDashboardUiState.Error)
    }
}
