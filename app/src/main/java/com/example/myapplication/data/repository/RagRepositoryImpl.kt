package com.example.myapplication.data.repository

import com.example.myapplication.data.mapper.*
import com.example.myapplication.data.remote.AskApi
import com.example.myapplication.data.remote.ChatApi
import com.example.myapplication.data.remote.RoleplayApi
import com.example.myapplication.data.remote.PronunciationApi
import com.example.myapplication.data.remote.dto.*
import com.example.myapplication.domain.model.*
import com.example.myapplication.domain.repository.RagRepository
import java.io.File

class RagRepositoryImpl(
    private val askApi: AskApi,
    private val chatApi: ChatApi,
    private val roleplayApi: RoleplayApi,
    private val pronunciationApi: PronunciationApi
) : RagRepository {
    override suspend fun askGrounded(query: String, k: Int, unit: String?, maxContextChars: Int): AskResp =
        askApi.ask(AskReqDto(query = query, k = k, unit = unit, maxContextChars = maxContextChars)).toDomain()

    override suspend fun chatFree(messages: List<ChatMsgDto>): ChatRespDto =
        chatApi.chat(ChatReqDto(messages = messages))

    override suspend fun ask(request: AskReq): AskResp =
        askApi.ask(AskReqDto(
            query = request.query,
            k = request.k,
            unit = request.unit,
            maxContextChars = request.contextTokenBudget
        )).toDomain()

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
