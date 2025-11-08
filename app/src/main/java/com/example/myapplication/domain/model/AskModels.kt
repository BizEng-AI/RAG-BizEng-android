package com.example.myapplication.domain.model
data class AskReq(
    val query: String,
    val k: Int = 5,
    val unit: String? = null,
    val contextTokenBudget: Int = 1200
)
data class AskResp(val answer: String, val sources: List<String>)
