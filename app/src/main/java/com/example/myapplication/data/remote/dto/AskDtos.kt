package com.example.myapplication.data.remote.dto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AskReqDto(
    @SerialName("query") val query: String,
    @SerialName("k") val k: Int = 5,
    @SerialName("max_context_chars") val maxContextChars: Int = 6000,
    @SerialName("unit") val unit: String? = null
)

@Serializable
data class AskRespDto(val answer: String, val sources: List<String>)
