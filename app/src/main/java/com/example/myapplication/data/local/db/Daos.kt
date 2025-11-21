package com.example.myapplication.data.local.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RoleplayScenarioDao {
    @Query("SELECT * FROM roleplay_scenarios")
    fun getAll(): Flow<List<RoleplayScenarioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<RoleplayScenarioEntity>)
}

@Dao
interface PronunciationDao {
    @Query("SELECT * FROM pronunciation_examples")
    fun getAll(): Flow<List<PronunciationExampleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<PronunciationExampleEntity>)
}

@Dao
interface ExerciseAttemptDao {
    @Query("SELECT * FROM exercise_attempts ORDER BY localId DESC LIMIT :limit")
    fun getRecent(limit: Int = 50): Flow<List<ExerciseAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ExerciseAttemptEntity>)

    @Query("DELETE FROM exercise_attempts")
    suspend fun clearAll()
}

