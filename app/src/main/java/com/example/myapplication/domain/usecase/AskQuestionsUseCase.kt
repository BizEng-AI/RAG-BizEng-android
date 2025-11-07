package com.example.myapplication.domain.usecase
import com.example.myapplication.domain.model.*
import com.example.myapplication.domain.repository.RagRepository

class AskQuestionUseCase(private val repo: RagRepository) {
    suspend operator fun invoke(req: AskReq): AskResp = repo.ask(req)
}
