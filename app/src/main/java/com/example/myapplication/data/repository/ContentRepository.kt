package com.example.myapplication.data.repository

import com.example.myapplication.data.local.db.PronunciationDao
import com.example.myapplication.data.local.db.PronunciationExampleEntity
import com.example.myapplication.data.local.db.RoleplayScenarioDao
import com.example.myapplication.data.local.db.RoleplayScenarioEntity
import kotlinx.coroutines.flow.Flow

class ContentRepository(
    private val roleplayDao: RoleplayScenarioDao,
    private val pronunciationDao: PronunciationDao
) {
    fun observeRoleplayScenarios(): Flow<List<RoleplayScenarioEntity>> = roleplayDao.getAll()
    suspend fun upsertRoleplayScenarios(items: List<RoleplayScenarioEntity>) = roleplayDao.upsertAll(items)

    fun observePronunciationExamples(): Flow<List<PronunciationExampleEntity>> = pronunciationDao.getAll()
    suspend fun upsertPronunciationExamples(items: List<PronunciationExampleEntity>) = pronunciationDao.upsertAll(items)
}

