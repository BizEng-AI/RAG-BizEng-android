package com.bizenglish.app.data.remote.dto
import kotlinx.serialization.Serializable

/**
 * Chat request DTO for /chat endpoint
 * Server expects ONLY messages array - no extra fields
 */
@Serializable
data class ChatReqDto(
    val messages: List<ChatMsgDto>
    // NOTE: k, maxContextChars, unit are for /ask endpoint only, NOT /chat
)
