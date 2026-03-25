package com.bizenglish.app.data.mapper
import com.bizenglish.app.data.remote.dto.*
import com.bizenglish.app.domain.model.*

fun AskReq.toDto() = AskReqDto(
    query = query,
    k = k,
    unit = unit,
    maxContextChars = contextTokenBudget
)

fun AskRespDto.toDomain() = AskResp(answer, sources)
