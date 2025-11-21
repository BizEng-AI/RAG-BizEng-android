package com.example.myapplication.data.remote

import com.example.myapplication.data.remote.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class TrackingApi(
    private val client: HttpClient,
    private val baseUrl: String
) {

    suspend fun startAttempt(request: ExerciseAttemptReq): ExerciseAttemptDto {
        return client.post("$baseUrl/tracking/attempts") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun updateAttempt(attemptId: String, update: ExerciseAttemptUpdate): ExerciseAttemptDto {
        return client.patch("$baseUrl/tracking/attempts/$attemptId") {
            contentType(ContentType.Application.Json)
            setBody(update)
        }.body()
    }

    suspend fun logEvent(request: ActivityEventReq): ActivityEventDto {
        return client.post("$baseUrl/tracking/events") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun getMyProgress(from: String? = null, to: String? = null): ProgressSummaryDto {
        return client.get("$baseUrl/tracking/my-progress") {
            from?.let { parameter("from", it) }
            to?.let { parameter("to", it) }
        }.body()
    }

    suspend fun getMyProgress(days: Int): ProgressSummaryDto {
        return client.get("$baseUrl/tracking/my-progress") {
            parameter("days", days)
        }.body()
    }

    suspend fun getMySummary(days: Int = 30): SummaryDto {
        return client.get("$baseUrl/tracking/summary") {
            parameter("days", days)
        }.body()
    }

    suspend fun getMyAttempts(limit: Int = 50, offset: Int = 0, days: Int = 30): List<AttemptDto> {
        return client.get("$baseUrl/tracking/my-attempts") {
            parameter("limit", limit)
            parameter("offset", offset)
            parameter("days", days)
        }.body()
    }
}
