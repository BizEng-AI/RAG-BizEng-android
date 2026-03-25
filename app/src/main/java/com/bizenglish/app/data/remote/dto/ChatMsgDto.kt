package com.bizenglish.app.data.remote.dto
import kotlinx.serialization.Serializable

@Serializable
data class ChatMsgDto(val role: String, val content: String)

