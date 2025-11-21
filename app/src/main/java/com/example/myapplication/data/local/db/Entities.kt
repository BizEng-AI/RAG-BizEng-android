package com.example.myapplication.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "roleplay_scenarios")
data class RoleplayScenarioEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String
)

@Entity(tableName = "pronunciation_examples")
data class PronunciationExampleEntity(
    @PrimaryKey val id: String,
    val text: String,
    val ipa: String?,
    val locale: String
)

@Entity(tableName = "exercise_attempts")
data class ExerciseAttemptEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val remoteId: Int?,
    val userId: Int?,
    val exerciseType: String,
    val exerciseId: String?,
    val startedAt: String,
    val finishedAt: String?,
    val durationSeconds: Int?,
    val score: Double?
)

