package com.example.myapplication.data.remote.dto
import kotlinx.serialization.Serializable

@Serializable
data class ChatMsgDto(val role: String, val content: String)

