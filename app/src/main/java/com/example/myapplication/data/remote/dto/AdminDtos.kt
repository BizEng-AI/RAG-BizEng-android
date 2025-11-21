package com.example.myapplication.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class DayCountDto(
    @SerialName("day") @JsonNames("date") val day: String = "",
    // server may use keys like `count`, `value`, `events`, `total` or omit the field entirely
    @SerialName("count") @JsonNames("events", "value", "total") val count: Int = 0
)

@Serializable
data class ActiveTodayDto(
    val date: String = "",
    @SerialName("active_students") val activeStudents: Int = 0
)

@Serializable
data class RoleCountDto(
    val role: String = "",
    val count: Int = 0
)

@Serializable
data class RefreshTokenStatsDto(
    val total: Int = 0,
    val active: Int = 0,
    val revoked: Int = 0
)

@Serializable
data class OverviewTotalsDto(
    @SerialName("total_users") val totalUsers: Int? = null,
    @SerialName("total_attempts") val totalAttempts: Int? = null
)

@Serializable
data class AdminOverviewDto(
    @SerialName("activity_events") val activityEvents: List<DayCountDto> = emptyList(),
    @SerialName("exercise_attempts") val exerciseAttempts: List<DayCountDto> = emptyList(),
    @SerialName("user_signups") val userSignups: List<DayCountDto> = emptyList(),
    val roles: List<RoleCountDto> = emptyList(),
    @SerialName("refresh_tokens") val refreshTokens: RefreshTokenStatsDto? = null,
    val totals: OverviewTotalsDto? = null,
    @SerialName("last_updated_at") val lastUpdatedAtMillis: Long = System.currentTimeMillis()
)

@Serializable
data class RecentAttemptDto(
    @SerialName("attempt_id") val attemptId: Int = 0,
    @SerialName("student_email") val studentEmail: String = "",
    @SerialName("student_name") val studentName: String? = null,
    @SerialName("exercise_type") val exerciseType: String = "",
    @SerialName("exercise_id") val exerciseId: String? = null,
    val score: Float? = null,
    @SerialName("duration_seconds") val durationSeconds: Int? = null,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("finished_at") val finishedAt: String? = null
)

// Per-user activity timeline
@Serializable
data class UserSummaryDto(
    val id: Long = 0L,
    val email: String = "",
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("group_name") val groupName: String? = null
)

@Serializable
data class UserActivityItemDto(
    @SerialName("attempt_id") val attemptId: Long = 0L,
    @SerialName("exercise_type") val exerciseType: String = "",
    @SerialName("exercise_id") val exerciseId: String? = null,
    @SerialName("duration_seconds") val durationSeconds: Int? = null,
    @SerialName("pronunciation_score") val pronunciationScore: Float? = null,
    val score: Float? = null,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("finished_at") val finishedAt: String? = null
)

@Serializable
data class UserActivityResponse(
    val user: UserSummaryDto = UserSummaryDto(),
    val items: List<UserActivityItemDto> = emptyList()
)

// Per-user aggregated stats
@Serializable
data class UserActivitySummaryDto(
    @SerialName("user_id") val userId: Long = 0L,
    val email: String = "",
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("group_name") val groupName: String? = null,
    @SerialName("total_exercises") val totalExercises: Int = 0,
    @SerialName("pronunciation_count") val pronunciationCount: Int = 0,
    @SerialName("chat_count") val chatCount: Int = 0,
    @SerialName("roleplay_count") val roleplayCount: Int = 0,
    @SerialName("total_duration_seconds") val totalDurationSeconds: Int = 0,
    @SerialName("avg_pronunciation_score") val avgPronunciationScore: Float? = null
)

// Per-group aggregated stats
@Serializable
data class GroupActivitySummaryDto(
    @SerialName("group_name") val groupName: String? = null,
    @SerialName("student_count") val studentCount: Int = 0,
    @SerialName("total_exercises") val totalExercises: Int = 0,
    @SerialName("pronunciation_count") val pronunciationCount: Int = 0,
    @SerialName("chat_count") val chatCount: Int = 0,
    @SerialName("roleplay_count") val roleplayCount: Int = 0,
    @SerialName("total_duration_seconds") val totalDurationSeconds: Int = 0,
    @SerialName("avg_pronunciation_score") val avgPronunciationScore: Float? = null
)
