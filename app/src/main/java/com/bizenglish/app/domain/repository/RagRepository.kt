package com.bizenglish.app.domain.repository

import com.bizenglish.app.domain.model.AskReq
import com.bizenglish.app.domain.model.AskResp
import com.bizenglish.app.data.remote.dto.ChatMsgDto
import com.bizenglish.app.data.remote.dto.ChatRespDto
import com.bizenglish.app.data.remote.dto.RoleplayStartRespDto
import com.bizenglish.app.data.remote.dto.RoleplayTurnRespDto
import com.bizenglish.app.data.remote.dto.PronunciationResultDto
import com.bizenglish.app.data.remote.dto.PronunciationQuickCheckDto
import java.io.File

interface RagRepository {
    suspend fun ask(request: AskReq): AskResp
    suspend fun askGrounded(query: String, k: Int = 6, unit: String? = null, maxContextChars: Int = 1200): AskResp
    suspend fun chatFree(messages: List<ChatMsgDto>): ChatRespDto

    // New session-based roleplay methods
    suspend fun startRoleplay(scenarioId: String, useRag: Boolean = true): RoleplayStartRespDto
    suspend fun submitRoleplayTurn(sessionId: String, studentMessage: String): RoleplayTurnRespDto

    // Pronunciation assessment
    suspend fun assessPronunciation(audioFile: File, referenceText: String): PronunciationResultDto
    suspend fun quickPronunciationCheck(audioFile: File, referenceText: String): PronunciationQuickCheckDto

    suspend fun getLatestUpdate(): String
    suspend fun getHealth(): String
}