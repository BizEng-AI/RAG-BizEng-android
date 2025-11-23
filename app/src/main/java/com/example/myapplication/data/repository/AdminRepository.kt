package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.core.network.AuthInterceptor
import com.example.myapplication.data.remote.AdminApi
import com.example.myapplication.data.remote.dto.*
import io.ktor.client.plugins.*
import io.ktor.http.*
import javax.inject.Inject

interface UnauthorizedRetryHandler {
    suspend fun <T> runWithRefresh(block: suspend () -> T): T
}

class AuthInterceptorRetryHandler(
    private val authInterceptor: AuthInterceptor
) : UnauthorizedRetryHandler {
    override suspend fun <T> runWithRefresh(block: suspend () -> T): T {
        return try {
            Log.d("AdminRepository", "runWithRefresh: starting block")
            block()
        } catch (e: ClientRequestException) {
            Log.w("AdminRepository", "runWithRefresh: caught ${'$'}{e.response.status}")
            if (e.response.status == HttpStatusCode.Unauthorized) {
                Log.w("AdminRepository", "runWithRefresh: 401 Unauthorized, attempting token refresh")
                val refreshSucceeded = authInterceptor.handleUnauthorized()
                if (refreshSucceeded) {
                    Log.d("AdminRepository", "runWithRefresh: refresh succeeded, retrying block")
                    block()
                } else {
                    Log.e("AdminRepository", "runWithRefresh: refresh failed, throwing session expired")
                    throw IllegalStateException("Session expired. Please log in again.")
                }
            } else {
                Log.e("AdminRepository", "runWithRefresh: non-401 error", e)
                throw e
            }
        }
    }
}

class AdminRepository @Inject constructor(
    private val adminApi: AdminApi,
    private val retryHandler: UnauthorizedRetryHandler
) {
    private companion object {
        const val TAG = "AdminRepository"
    }

    suspend fun getOverview(): Result<AdminOverviewDto> =
        executeWithRefresh("overview") { adminApi.getOverview() }

    suspend fun getActiveToday(): Result<ActiveTodayDto> =
        executeWithRefresh("active_today") { adminApi.getActiveToday() }

    suspend fun getAttemptsDaily(): Result<List<DayCountDto>> =
        executeWithRefresh("attempts_daily") { adminApi.getAttemptsDaily() }

    suspend fun getUsersSignupsDaily(): Result<List<DayCountDto>> =
        executeWithRefresh("users_signups_daily") { adminApi.getUsersSignupsDaily() }

    suspend fun getRecentAttempts(limit: Int = 7): Result<List<RecentAttemptDto>> =
        executeWithRefresh("recent_attempts") { adminApi.getRecentAttempts(limit) }

    suspend fun getUserActivity(userId: Long, days: Int = 30): Result<UserActivityResponse> =
        executeWithRefresh("user_activity/${'$'}userId") { adminApi.getUserActivity(userId, days) }

    suspend fun getUsersActivity(days: Int = 30): Result<List<UserActivitySummaryDto>> =
        executeWithRefresh("users_activity") { adminApi.getUsersActivity(days) }

    suspend fun getGroupsActivity(days: Int = 30): Result<List<GroupActivitySummaryDto>> =
        executeWithRefresh("groups_activity") { adminApi.getGroupsActivity(days) }

    private suspend fun <T> executeWithRefresh(
        endpoint: String,
        block: suspend () -> T
    ): Result<T> = runCatching {
        Log.d(TAG, "➡ [${'$'}endpoint] Request started")
        retryHandler.runWithRefresh {
            val result = block()
            Log.d(TAG, "✅ [${'$'}endpoint] Request succeeded")
            result
        }
    }.also { result ->
        if (result.isFailure) {
            Log.e(TAG, "❌ [${'$'}endpoint] Final result failed: ${'$'}{result.exceptionOrNull()?.message}", result.exceptionOrNull())
        }
    }
}
