package com.example.myapplication.data.remote

import android.util.Log
import com.example.myapplication.data.remote.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class AdminApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    companion object {
        private const val TAG = "📊 AdminApi"
    }

    suspend fun getOverview(): AdminOverviewDto {
        Log.d(TAG, "GET /admin/monitor/overview")
        return client.get("$baseUrl/admin/monitor/overview")
            .body()
    }

    suspend fun getAttemptsDaily(): List<DayCountDto> {
        Log.d(TAG, "GET /admin/monitor/attempts_daily")
        return client.get("$baseUrl/admin/monitor/attempts_daily")
            .body()
    }

    suspend fun getUsersSignupsDaily(): List<DayCountDto> {
        Log.d(TAG, "GET /admin/monitor/users_signups_daily")
        return client.get("$baseUrl/admin/monitor/users_signups_daily")
            .body()
    }

    suspend fun getActiveToday(): ActiveTodayDto {
        Log.d(TAG, "GET /admin/monitor/active_today")
        return client.get("$baseUrl/admin/monitor/active_today")
            .body()
    }

    suspend fun getRecentAttempts(limit: Int = 7): List<RecentAttemptDto> {
        Log.d(TAG, "GET /admin/monitor/recent_attempts?limit=$limit")
        return client.get("$baseUrl/admin/monitor/recent_attempts") {
            parameter("limit", limit)
        }.body()
    }

    // Per-user activity timeline
    suspend fun getUserActivity(userId: Long, days: Int = 30): UserActivityResponse {
        Log.d(TAG, "GET /admin/monitor/user_activity/$userId?days=$days")
        return client.get("$baseUrl/admin/monitor/user_activity/$userId") {
            parameter("days", days)
        }.body()
    }

    // Per-user aggregated stats
    suspend fun getUsersActivity(days: Int = 30): List<UserActivitySummaryDto> {
        Log.d(TAG, "GET /admin/monitor/users_activity?days=$days")
        return client.get("$baseUrl/admin/monitor/users_activity") {
            parameter("days", days)
        }.body()
    }

    // Per-group aggregated stats
    suspend fun getGroupsActivity(days: Int = 30): List<GroupActivitySummaryDto> {
        Log.d(TAG, "GET /admin/monitor/groups_activity?days=$days")
        return client.get("$baseUrl/admin/monitor/groups_activity") {
            parameter("days", days)
        }.body()
    }
}

