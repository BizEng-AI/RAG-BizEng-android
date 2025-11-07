package com.example.myapplication.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// NEW: Phoneme-level details for each sound in a word
@Serializable
data class PronunciationPhonemeDto(
    val phoneme: String,    // IPA symbol: "ɔː", "θ", "ɪ", etc.
    val score: Float        // 0-100 accuracy for this specific sound
)

// UPDATED: Enhanced word details with IPA transcription and phonemes
@Serializable
data class PronunciationWordDto(
    val word: String,
    @SerialName("accuracy_score") val accuracyScore: Float,  // 0-100
    @SerialName("error_type") val errorType: String? = null,  // "Mispronunciation", "Omission", "Insertion", or null
    val feedback: String? = null,  // NEW: Word-specific feedback
    val phonemes: List<PronunciationPhonemeDto>? = null,  // NEW: Phoneme breakdown
    @SerialName("ipa_expected") val ipaExpected: String? = null,  // NEW: Full IPA transcription (e.g., "ˈmɔːnɪŋ")
    @SerialName("ipa_actual") val ipaActual: String? = null  // NEW: User's actual pronunciation (future use)
)

// UPDATED: Enhanced pronunciation result with detailed feedback
@Serializable
data class PronunciationResultDto(
    val transcript: String,  // What the user actually said
    @SerialName("accuracy_score") val accuracyScore: Float,  // 0-100 overall accuracy
    @SerialName("fluency_score") val fluencyScore: Float,  // 0-100 how natural it sounds
    @SerialName("completeness_score") val completenessScore: Float,  // 0-100 did they say everything
    @SerialName("pronunciation_score") val pronunciationScore: Float,  // 0-100 overall pronunciation
    val words: List<PronunciationWordDto>,  // Individual word scores with IPA
    val feedback: String,  // Human-readable main feedback
    @SerialName("detailed_feedback") val detailedFeedback: List<String>? = null  // NEW: Step-by-step tips
)

@Serializable
data class PronunciationQuickCheckDto(
    val score: Float,  // 0-100
    val feedback: String,
    val transcript: String,
    val needs_practice: Boolean,
    val mispronounced_words: List<String>
)

