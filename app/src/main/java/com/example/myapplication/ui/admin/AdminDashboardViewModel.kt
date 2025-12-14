package com.example.myapplication.ui.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.remote.dto.ActiveTodayDto
import com.example.myapplication.data.remote.dto.AdminOverviewDto
import com.example.myapplication.data.remote.dto.DayCountDto
import com.example.myapplication.data.remote.dto.RecentAttemptDto
import com.example.myapplication.data.remote.dto.UserActivitySummaryDto
import com.example.myapplication.data.remote.dto.GroupActivitySummaryDto
import com.example.myapplication.data.remote.dto.UserActivityResponse
import com.example.myapplication.data.repository.AdminRepository
import com.example.myapplication.di.CoroutinesModule.IODispatcher
import com.example.myapplication.ui.common.UiErrorMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import javax.inject.Inject
import javax.net.ssl.SSLHandshakeException

sealed interface AdminDashboardUiState {
    object Loading : AdminDashboardUiState
    data class Success(val data: AdminDashboardData) : AdminDashboardUiState
    data class Error(val message: String) : AdminDashboardUiState
}

data class AdminDashboardData(
    val overview: AdminOverviewDto? = null,
    val attempts: List<DayCountDto> = emptyList(),
    val signups: List<DayCountDto> = emptyList(),
    val activeToday: ActiveTodayDto? = null,
    val recentAttempts: List<RecentAttemptDto> = emptyList(),
    val usersActivity: List<UserActivitySummaryDto> = emptyList(),
    val groupsActivity: List<GroupActivitySummaryDto> = emptyList(),
    val lastUpdatedAtMillis: Long = System.currentTimeMillis()
)

// New: sections for dashboard
enum class AdminSection { Overview, Students, Groups, RecentAttempts }

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val repository: AdminRepository,
    @IODispatcher private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    companion object {
        private const val TAG = "AdminDashboardVM"
        private const val MIN_FETCH_INTERVAL_MS = 60_000L
    }

    private val _uiState = MutableStateFlow<AdminDashboardUiState>(AdminDashboardUiState.Loading)
    val uiState: StateFlow<AdminDashboardUiState> = _uiState

    // New: selected section state
    private val _selectedSection = MutableStateFlow(AdminSection.Overview)
    val selectedSection: StateFlow<AdminSection> = _selectedSection

    private var refreshJob: Job? = null
    private var lastFetchAt = 0L

    init {
        Log.d(TAG, "init: starting initial dashboard load")
        loadDashboard()
    }

    // Replace mapAdminError logic with centralized mapper
    private fun mapAdminError(t: Throwable?): String = UiErrorMapper.mapAdminError(t)

    fun loadDashboard(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && lastFetchAt != 0L && now - lastFetchAt < MIN_FETCH_INTERVAL_MS) {
            Log.d(TAG, "⏱ Skipping fetch (cached <60s). now=$now lastFetchAt=$lastFetchAt")
            return
        }

        refreshJob?.cancel()
        refreshJob = viewModelScope.launch(dispatcher) {
            Log.d(TAG, "🚀 Loading admin dashboard (force=$force)")
            _uiState.value = AdminDashboardUiState.Loading
            try {
                val overview = repository.getOverview()
                val attempts = repository.getAttemptsDaily()
                val signups = repository.getUsersSignupsDaily()
                val activeToday = repository.getActiveToday()
                val recentAttempts = repository.getRecentAttempts(limit = 7)
                val usersActivity = repository.getUsersActivity(days = 30)
                val groupsActivity = repository.getGroupsActivity(days = 30)
                val failures = listOf(overview, attempts, signups, activeToday, recentAttempts, usersActivity, groupsActivity).filter { it.isFailure }
                if (failures.isEmpty()) {
                    val data = AdminDashboardData(
                        overview = overview.getOrNull(),
                        attempts = attempts.getOrDefault(emptyList()),
                        signups = signups.getOrDefault(emptyList()),
                        activeToday = activeToday.getOrNull(),
                        recentAttempts = recentAttempts.getOrDefault(emptyList()),
                        usersActivity = usersActivity.getOrDefault(emptyList()),
                        groupsActivity = groupsActivity.getOrDefault(emptyList()),
                        lastUpdatedAtMillis = System.currentTimeMillis()
                    )
                    lastFetchAt = data.lastUpdatedAtMillis
                    _uiState.value = AdminDashboardUiState.Success(data)
                } else {
                    val firstErr = failures.first().exceptionOrNull()
                    _uiState.value = AdminDashboardUiState.Error(mapAdminError(firstErr))
                }
            } catch (t: Throwable) {
                _uiState.value = AdminDashboardUiState.Error(mapAdminError(t))
            }
        }
    }

    fun setSection(section: AdminSection) {
        Log.d(TAG, "📄 Section selected: ${'$'}section")
        _selectedSection.value = section
    }

    fun retry() {
        Log.d(TAG, "🔁 Manual retry triggered")
        loadDashboard(force = true)
    }

    // Fetch individual user activity timeline
    suspend fun getUserActivity(userId: Long, days: Int = 30): Result<UserActivityResponse> {
        Log.d(TAG, "📈 Loading user activity for userId=${'$'}userId, days=${'$'}days")
        return repository.getUserActivity(userId, days)
    }
}
