package com.example.myapplication.data.mapper
import com.example.myapplication.data.remote.dto.*
import com.example.myapplication.domain.model.*

fun AskReq.toDto() = AskReqDto(
    query = query,
    k = k,
    unit = unit,
    maxContextChars = contextTokenBudget
)

fun AskRespDto.toDomain() = AskResp(answer, sources)
