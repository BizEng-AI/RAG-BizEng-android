package com.bizenglish.app.data.remote.dto
import kotlinx.serialization.Serializable

/**
 * Chat response DTO for /chat endpoint
 * Server returns "answer" field with AI response
 */
@Serializable
data class ChatRespDto(
    val answer: String,
    val sources: List<String> = emptyList()
) {
    // Helper method for compatibility
    fun getResponseText(): String = answer
}

