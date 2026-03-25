package com.bizenglish.app.domain.usecase
import com.bizenglish.app.domain.model.*
import com.bizenglish.app.domain.repository.RagRepository

class AskQuestionUseCase(private val repo: RagRepository) {
    suspend operator fun invoke(req: AskReq): AskResp = repo.ask(req)
}
