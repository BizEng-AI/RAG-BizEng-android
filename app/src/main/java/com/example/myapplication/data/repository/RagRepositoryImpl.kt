package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.mapper.*
import com.example.myapplication.data.remote.AskApi
import com.example.myapplication.data.remote.ChatApi
import com.example.myapplication.data.remote.RoleplayApi
import com.example.myapplication.data.remote.PronunciationApi
import com.example.myapplication.data.remote.dto.*
import com.example.myapplication.domain.model.*
import com.example.myapplication.domain.repository.RagRepository
import com.example.myapplication.ui.common.UiErrorMapper
import java.io.File

class RagRepositoryImpl(
    private val askApi: AskApi,
    private val chatApi: ChatApi,
    private val roleplayApi: RoleplayApi,
    private val pronunciationApi: PronunciationApi
) : RagRepository {
    override suspend fun askGrounded(query: String, k: Int, unit: String?, maxContextChars: Int): AskResp = try {
        askApi.ask(AskReqDto(query = query, k = k, unit = unit, maxContextChars = maxContextChars)).toDomain()
    } catch (t: Throwable) {
        val sanitized = UiErrorMapper.mapChatError(t)
        Log.d("RagRepo", "guardrail askGrounded failure raw='${t.message}' sanitized='$sanitized'")
        AskResp(answer = sanitized, sources = emptyList())
    }

    override suspend fun chatFree(messages: List<ChatMsgDto>): ChatRespDto = try {
        chatApi.chat(ChatReqDto(messages = messages))
    } catch (t: Throwable) {
        val sanitized = UiErrorMapper.mapChatError(t)
        Log.d("RagRepo", "guardrail chatFree failure raw='${t.message}' sanitized='$sanitized'")
        ChatRespDto(answer = sanitized, sources = emptyList())
    }

    override suspend fun ask(request: AskReq): AskResp = try {
        askApi.ask(AskReqDto(
            query = request.query,
            k = request.k,
            unit = request.unit,
            maxContextChars = request.contextTokenBudget
        )).toDomain()
    } catch (t: Throwable) {
        val sanitized = UiErrorMapper.mapChatError(t)
        Log.d("RagRepo", "guardrail ask failure raw='${t.message}' sanitized='$sanitized'")
        AskResp(answer = sanitized, sources = emptyList())
    }

    override suspend fun getLatestUpdate(): String = askApi.getLatestUpdate()

    override suspend fun startRoleplay(scenarioId: String, useRag: Boolean): RoleplayStartRespDto =
        roleplayApi.startRoleplay(scenarioId, useRag)

    override suspend fun submitRoleplayTurn(sessionId: String, studentMessage: String): RoleplayTurnRespDto =
        roleplayApi.submitTurn(sessionId, studentMessage)

    override suspend fun assessPronunciation(audioFile: File, referenceText: String): PronunciationResultDto =
        pronunciationApi.assessPronunciation(audioFile, referenceText)

    override suspend fun quickPronunciationCheck(audioFile: File, referenceText: String): PronunciationQuickCheckDto =
        pronunciationApi.quickCheck(audioFile, referenceText)

    override suspend fun getHealth(): String = askApi.getHealth()
}
