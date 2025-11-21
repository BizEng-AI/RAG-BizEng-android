package com.example.myapplication.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// ============================================================================
// EXERCISE TRACKING DTOs
// ============================================================================

@Serializable
data class ExerciseAttemptReq(
    @SerialName("exercise_id") val exerciseId: String,
    @SerialName("exercise_type") val exerciseType: String  // "chat", "roleplay", "pronunciation", "rag"
)

@Serializable
data class ExerciseAttemptDto(
    val id: String,
    @SerialName("exercise_id") val exerciseId: String,
    @SerialName("exercise_type") val exerciseType: String,
    val status: String,  // "started", "completed", "abandoned"
    val score: Float? = null,
    @SerialName("duration_sec") val durationSec: Int? = null,
    @SerialName("started_at") val startedAt: String,
    @SerialName("finished_at") val finishedAt: String? = null
)

@Serializable
data class ExerciseAttemptUpdate(
    val status: String? = null,
    val score: Float? = null,
    @SerialName("duration_sec") val durationSec: Int? = null
)

@Serializable
data class ActivityEventReq(
    @SerialName("exercise_id") val exerciseId: String,
    @SerialName("event_type") val eventType: String,  // "opened", "started", "completed", "abandoned"
    val payload: Map<String, String>? = null
)

@Serializable
data class ActivityEventDto(
    val id: Long,
    @SerialName("event_type") val eventType: String,
    val ts: String
)

// ============================================================================
// PROGRESS TRACKING DTOs
// ============================================================================

@Serializable
data class ProgressSummaryDto(
    val totals: TotalsDto = TotalsDto(),
    @SerialName("by_type") val byType: Map<String, TypeStatsDto> = emptyMap(),
    @SerialName("recent_attempts") val recentAttempts: List<ExerciseAttemptDto> = emptyList()
)

@Serializable
data class TotalsDto(
    val attempts: Int = 0,
    val completed: Int = 0,
    @SerialName("avg_score") val avgScore: Float = 0f,
    @SerialName("total_minutes") val totalMinutes: Int = 0
)

@Serializable
data class TypeStatsDto(
    val attempts: Int = 0,
    @SerialName("avg_score") val avgScore: Float = 0f
)

// ============================================================================
// ADMIN DTOs
// ============================================================================

@Serializable
data class AdminDashboardDto(
    @SerialName("total_students") val totalStudents: Int,
    @SerialName("active_today") val activeToday: Int,
    @SerialName("total_attempts") val totalAttempts: Int,
    @SerialName("avg_score") val avgScore: Float,
    @SerialName("top_performers") val topPerformers: List<TopPerformerDto>
)

@Serializable
data class TopPerformerDto(
    @SerialName("user_id") val userId: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("avg_score") val avgScore: Float
)

@Serializable
data class StudentsListDto(
    val total: Int,
    val students: List<StudentSummaryDto>
)

@Serializable
data class StudentSummaryDto(
    val id: String,
    val email: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("last_active") val lastActive: String? = null,
    @SerialName("total_attempts") val totalAttempts: Int,
    @SerialName("avg_score") val avgScore: Float? = null
)

@Serializable
data class StudentProgressDto(
    val user: UserDto,
    val totals: TotalsDto,
    @SerialName("by_day") val byDay: List<DayStatsDto>,
    @SerialName("by_type") val byType: Map<String, TypeStatsDto>,
    @SerialName("recent_attempts") val recentAttempts: List<ExerciseAttemptDto>
)

@Serializable
data class DayStatsDto(
    val date: String,
    val attempts: Int,
    val completed: Int,
    @SerialName("avg_score") val avgScore: Float,
    val minutes: Int
)

// USER ANALYTICS (Student-facing) NEW DTOs

@Serializable
data class AttemptDto(
    val id: Int,
    @SerialName("user_id") val userId: Int,
    @SerialName("exercise_type") val exerciseType: String,
    @SerialName("exercise_id") val exerciseId: String? = null,
    @SerialName("started_at") val startedAt: String,
    @SerialName("finished_at") val finishedAt: String? = null,
    @SerialName("duration_seconds") val durationSeconds: Int? = null,
    val score: Double? = null,
    val passed: Boolean? = null,
    @SerialName("extra_metadata") val extraMetadata: Map<String, JsonElement>? = null
)

@Serializable
data class SummaryDto(
    @SerialName("user_id") val userId: Int,
    val email: String,
    @SerialName("group_number") val groupNumber: String? = null,
    @SerialName("total_exercises") val totalExercises: Int,
    @SerialName("pronunciation_count") val pronunciationCount: Int,
    @SerialName("chat_count") val chatCount: Int,
    @SerialName("roleplay_count") val roleplayCount: Int,
    @SerialName("total_duration_seconds") val totalDurationSeconds: Int,
    @SerialName("avg_pronunciation_score") val avgPronunciationScore: Double?
)
