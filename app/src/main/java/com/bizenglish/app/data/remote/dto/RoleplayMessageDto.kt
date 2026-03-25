package com.bizenglish.app.data.remote.dto
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

// Message structure for roleplay
@Serializable
data class RoleplayMessageDto(
    val role: String,  // "user" | "assistant" | "system"
    val content: String
)

// Request to start a new roleplay session
@Serializable
data class RoleplayStartReqDto(
    @SerialName("scenario_id") val scenarioId: String,  // e.g., "job_interview"
    @SerialName("student_name") val studentName: String = "Student",
    @SerialName("use_rag") val useRag: Boolean = true
)

// Response from starting a roleplay session
@Serializable
data class RoleplayStartRespDto(
    @SerialName("session_id") val sessionId: String,
    @SerialName("scenario_title") val scenarioTitle: String,
    @SerialName("scenario_description") val scenarioDescription: String,
    val context: String,
    @SerialName("student_role") val studentRole: String,
    @SerialName("ai_role") val aiRole: String,
    @SerialName("current_stage") val currentStage: String = "opening",  // Default to "opening" if not provided
    @SerialName("initial_message") val initialMessage: String? = null  // AI's opening message, if provided
)

// Request to submit a turn in the roleplay
@Serializable
data class RoleplayTurnReqDto(
    @SerialName("session_id") val sessionId: String,
    val message: String  // Changed from student_message to message
)

// Individual error in a correction
@Serializable
data class ErrorDetailDto(
    val type: String,  // e.g., "grammar", "spelling", "word_choice"
    val incorrect: String,
    val correct: String,
    val explanation: String
)

// Correction object from the server
@Serializable
data class CorrectionDto(
    @SerialName("has_errors") val hasErrors: Boolean = false,
    val errors: List<ErrorDetailDto>? = null,
    val feedback: String? = null,

    // Legacy fields for backward compatibility
    @SerialName("error_type") val errorType: String? = null,
    val original: String? = null,
    val corrected: String? = null,
    val explanation: String? = null,
    val severity: String? = null
) {
    // Convert to a user-friendly string
    fun toDisplayString(): String? {
        // DEBUG: Log what we're working with
        android.util.Log.d("CORRECTION_DTO", "toDisplayString called: hasErrors=$hasErrors, errors=${errors?.size ?: 0}, feedback=$feedback")

        // New format with multiple errors
        if (hasErrors) {
            if (!errors.isNullOrEmpty()) {
                val errorMessages = errors.map { error ->
                    "❌ ${error.type.replaceFirstChar { it.uppercase() }}: '${error.incorrect}' → '${error.correct}'\n   ${error.explanation}"
                }
                val allErrors = errorMessages.joinToString("\n\n")
                val result = if (feedback != null) {
                    "$allErrors\n\n💡 $feedback"
                } else {
                    allErrors
                }
                android.util.Log.d("CORRECTION_DTO", "Returning NEW format with ${errors.size} errors")
                return result
            } else if (feedback != null) {
                // Has errors but no error details, show feedback
                android.util.Log.d("CORRECTION_DTO", "hasErrors=true but errors list empty, showing feedback")
                return "⚠️ $feedback"
            } else {
                // Has errors but no details - shouldn't happen but handle it
                android.util.Log.w("CORRECTION_DTO", "hasErrors=true but no errors or feedback!")
                return "⚠️ Please review your response for professionalism."
            }
        }

        // Legacy single error format (for backward compatibility)
        if (explanation != null || corrected != null || original != null) {
            android.util.Log.d("CORRECTION_DTO", "Using LEGACY format")
            return when {
                explanation != null && corrected != null ->
                    "❌ Correction: '$original' → '$corrected'\n   $explanation"
                explanation != null ->
                    "💡 $explanation"
                corrected != null && original != null ->
                    "❌ Try saying: '$corrected' instead of '$original'"
                else -> explanation
            }
        }

        // If just feedback without errors flag, show it
        if (feedback != null) {
            android.util.Log.d("CORRECTION_DTO", "Showing feedback only")
            return "💡 $feedback"
        }

        // Nothing to show
        android.util.Log.d("CORRECTION_DTO", "No correction to display (returning null)")
        return null
    }
}

// Response from submitting a turn
@Serializable
data class RoleplayTurnRespDto(
    @SerialName("ai_message") val aiMessage: String,
    val correction: CorrectionDto? = null,  // Changed from String to CorrectionDto
    @SerialName("current_stage") val currentStage: String = "development",  // Default to "development" if not provided
    @SerialName("is_completed") val isCompleted: Boolean = false,
    val feedback: String? = null  // Additional feedback if provided
)

// Legacy DTOs for backward compatibility (can be removed later)
@Serializable
data class RoleplayReqDto(
    val messages: List<RoleplayMessageDto>,
    val scenario: String? = null,
    val useRag: Boolean = true
)

@Serializable
data class RoleplayRespDto(
    val message: String,
    val correction: String? = null,
    val sources: List<String> = emptyList()
)

