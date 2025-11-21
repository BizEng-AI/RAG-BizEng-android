package com.example.myapplication.uiPack.roleplay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import com.example.myapplication.data.remote.dto.RoleplayMessageDto
import com.example.myapplication.domain.repository.RagRepository
import com.example.myapplication.voice.SpeechToTextController
import com.example.myapplication.voice.TextToSpeechController
import com.example.myapplication.data.repository.TrackingRepository
import com.example.myapplication.di.CoroutinesModule.IODispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RoleplayUiMsg(
    val id: String,
    val role: String,      // "user" | "assistant" | "system"
    val text: String,
    val correction: String? = null,  // Feedback from AI when user makes mistakes
    val streaming: Boolean = false
)

data class RoleplayUiState(
    val messages: List<RoleplayUiMsg> = emptyList(),
    val input: String = "",
    val scenario: String = "client_meeting",
    val useRag: Boolean = true,
    val recording: Boolean = false,
    val sending: Boolean = false,
    val error: String? = null,
    val sessionStarted: Boolean = false,
    val sessionId: String? = null,
    val currentStage: String? = null,
    val stageDescription: String? = null,
    val attemptTrackingId: String? = null // tracking attempt id
)

@HiltViewModel
class RoleplayVm @Inject constructor(
     private val repo: RagRepository,
     private val stt: SpeechToTextController,
     private val tts: TextToSpeechController,
     private val trackingRepository: TrackingRepository,
     @IODispatcher private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _state = MutableStateFlow(RoleplayUiState())
    val state: StateFlow<RoleplayUiState> = _state.asStateFlow()

    // Available scenarios - matching server's scenario IDs
    val scenarios = listOf(
        "job_interview" to "Job Interview",
        "client_meeting" to "Client Meeting",
        "customer_complaint" to "Customer Complaint",
        "team_meeting" to "Team Meeting",
        "business_call" to "Business Phone Call"  // Fixed: server expects "business_call" not "business_phone_call"
    )

    private var attemptCompleted = false
    private var lastTurnCompletedFlag = false

    fun onInputChange(v: String) = _state.update { it.copy(input = v, error = null) }

    fun setScenario(scenario: String) {
        android.util.Log.d("DEBUG_ROLEPLAY", "setScenario called with: $scenario")
        android.util.Log.d("DEBUG_ROLEPLAY", "Current state before: scenario=${_state.value.scenario}, sessionStarted=${_state.value.sessionStarted}")
        _state.update { it.copy(scenario = scenario) }
        android.util.Log.d("DEBUG_ROLEPLAY", "Current state after: scenario=${_state.value.scenario}")
    }

    fun toggleRag() {
        android.util.Log.d("DEBUG_ROLEPLAY", "toggleRag called. Current: ${_state.value.useRag}")
        _state.update { it.copy(useRag = !it.useRag) }
        android.util.Log.d("DEBUG_ROLEPLAY", "toggleRag after. New value: ${_state.value.useRag}")
    }

    // New function: Start session directly with a scenario
    fun startSessionWithScenario(scenario: String) {
        android.util.Log.d("DEBUG_ROLEPLAY", "startSessionWithScenario called with: $scenario")

        // Set the scenario first
        _state.update { it.copy(scenario = scenario) }

        // Then start the session immediately
        startSession()
    }

    fun startSession() {
        android.util.Log.d("DEBUG_ROLEPLAY", "startSession called")
        android.util.Log.d("DEBUG_ROLEPLAY", "Current sessionStarted: ${state.value.sessionStarted}")
        android.util.Log.d("DEBUG_ROLEPLAY", "Current scenario: ${state.value.scenario}")

        if (state.value.sessionStarted) {
            android.util.Log.d("DEBUG_ROLEPLAY", "Session already started, returning")
            return
        }

        viewModelScope.launch(dispatcher) {
            _state.update { it.copy(sending = true, error = null) }

            try {
                android.util.Log.d("DEBUG_ROLEPLAY", "Calling /roleplay/start with scenario: ${state.value.scenario}")

                val response = repo.startRoleplay(
                    scenarioId = state.value.scenario,
                    useRag = state.value.useRag
                )

                val attemptId = state.value.attemptTrackingId ?: trackingRepository
                    .startExercise(state.value.scenario, "roleplay")
                    .getOrNull()
                    ?.id

                android.util.Log.d("DEBUG_ROLEPLAY", "Session started successfully!")
                android.util.Log.d("DEBUG_ROLEPLAY", "Session ID: ${response.sessionId}")
                android.util.Log.d("DEBUG_ROLEPLAY", "Scenario: ${response.scenarioTitle}")
                android.util.Log.d("DEBUG_ROLEPLAY", "Context: ${response.context}")
                android.util.Log.d("DEBUG_ROLEPLAY", "Student role: ${response.studentRole}")
                android.util.Log.d("DEBUG_ROLEPLAY", "AI role: ${response.aiRole}")
                android.util.Log.d("DEBUG_ROLEPLAY", "Current stage: ${response.currentStage}")

                // Create initial greeting message
                val greeting = response.initialMessage ?:
                    "Welcome to the ${response.scenarioTitle}. ${response.context} I'm the ${response.aiRole}. Let's begin."

                android.util.Log.d("DEBUG_ROLEPLAY", "AI greeting: $greeting")

                // Update state with session info and AI's initial message
                _state.update {
                    it.copy(
                        sessionStarted = true,
                        sessionId = response.sessionId,
                        currentStage = response.currentStage,
                        stageDescription = response.scenarioDescription,
                        messages = listOf(
                            RoleplayUiMsg(
                                id = gen(),
                                role = "assistant",
                                text = greeting,
                                streaming = false
                            )
                        ),
                        sending = false,
                        attemptTrackingId = attemptId
                    )
                }

                // Removed auto-play - user can tap speaker button to hear greeting
                // tts.speak(greeting)

            } catch (t: Throwable) {
                android.util.Log.e("DEBUG_ROLEPLAY", "Error starting session: ${t.message}", t)

                val errorMsg = when {
                    t.message?.contains("404") == true ->
                        "Server error: /roleplay/start endpoint not found. Check your server."
                    t.message?.contains("500") == true ->
                        "Server error: Something went wrong. Check server console."
                    t.message?.contains("Connection refused") == true || t.message?.contains("failed to connect") == true ->
                        "Cannot connect to server. Check ADB forwarding and server status."
                    else -> "Error: ${t.message}"
                }

                _state.update { it.copy(sending = false, error = errorMsg) }
            }
        }
    }

    fun resetSession() {
        val attemptId = state.value.attemptTrackingId
        if (attemptId != null && !attemptCompleted) {
            attemptCompleted = true
            viewModelScope.launch {
                if (lastTurnCompletedFlag) {
                    trackingRepository.updateExercise(
                        attemptId = attemptId,
                        status = "completed",
                        score = null,
                        durationSec = null
                    )
                } else {
                    trackingRepository.abandonExercise(
                        attemptId = attemptId,
                        exerciseId = state.value.scenario,
                        exerciseType = "roleplay",
                        score = null
                    )
                }
            }
        }
        _state.update {
            RoleplayUiState(
                scenario = it.scenario,
                useRag = it.useRag,
                sessionId = null,
                currentStage = null,
                stageDescription = null
            )
        }
    }


    fun send() {
        val text = state.value.input.trim()
        if (text.isEmpty() || state.value.sending) return
        _state.update { it.copy(input = "") }

        if (!state.value.sessionStarted) {
            android.util.Log.w("RoleplayVm", "Attempted to send without active session")
            return
        }

        sendInternal(text)
    }

    private fun sendInternal(text: String) {
        viewModelScope.launch(dispatcher) {
            val sessionId = state.value.sessionId
            if (sessionId == null) {
                android.util.Log.e("ROLEPLAY", "Cannot send message: No active session")
                _state.update { it.copy(error = "No active session. Please start a roleplay first.") }
                return@launch
            }

            android.util.Log.d("ROLEPLAY", "═══════════════════════════════════════")
            android.util.Log.d("ROLEPLAY", "SENDING ROLEPLAY TURN")
            android.util.Log.d("ROLEPLAY", "Session ID: $sessionId")
            android.util.Log.d("ROLEPLAY", "Student message: $text")
            android.util.Log.d("ROLEPLAY", "═══════════════════════════════════════")

            // Add user message
            _state.update {
                it.copy(
                    sending = true,
                    messages = it.messages + RoleplayUiMsg(id = gen(), role = "user", text = text)
                )
            }

            // Add streaming assistant message placeholder
            _state.update {
                it.copy(
                    messages = it.messages + RoleplayUiMsg(id = gen(), role = "assistant", text = "", streaming = true)
                )
            }

            try {
                // Call /roleplay/turn endpoint
                val response = repo.submitRoleplayTurn(
                    sessionId = sessionId,
                    studentMessage = text
                )

                android.util.Log.d("ROLEPLAY", "═══════════════════════════════════════")
                android.util.Log.d("ROLEPLAY", "✓ ROLEPLAY TURN SUCCESSFUL")
                android.util.Log.d("ROLEPLAY", "AI response: ${response.aiMessage}")
                android.util.Log.d("ROLEPLAY", "Correction object RAW: ${response.correction}")
                android.util.Log.d("ROLEPLAY", "Has errors: ${response.correction?.hasErrors}")
                android.util.Log.d("ROLEPLAY", "Error count: ${response.correction?.errors?.size ?: 0}")
                android.util.Log.d("ROLEPLAY", "Feedback field: ${response.correction?.feedback}")

                // Log ALL correction fields for debugging
                response.correction?.let { corr ->
                    android.util.Log.d("ROLEPLAY", "--- CORRECTION OBJECT DETAILS ---")
                    android.util.Log.d("ROLEPLAY", "  hasErrors: ${corr.hasErrors}")
                    android.util.Log.d("ROLEPLAY", "  errors list: ${corr.errors}")
                    android.util.Log.d("ROLEPLAY", "  feedback: ${corr.feedback}")
                    android.util.Log.d("ROLEPLAY", "  errorType (legacy): ${corr.errorType}")
                    android.util.Log.d("ROLEPLAY", "  original (legacy): ${corr.original}")
                    android.util.Log.d("ROLEPLAY", "  corrected (legacy): ${corr.corrected}")
                    android.util.Log.d("ROLEPLAY", "  explanation (legacy): ${corr.explanation}")
                }

                if (response.correction?.hasErrors == true) {
                    android.util.Log.d("ROLEPLAY", "⚠️ ERRORS DETECTED IN USER MESSAGE:")
                    response.correction.errors?.forEachIndexed { index, error ->
                        android.util.Log.d("ROLEPLAY", "  Error ${index + 1}:")
                        android.util.Log.d("ROLEPLAY", "    Type: ${error.type}")
                        android.util.Log.d("ROLEPLAY", "    Wrong: '${error.incorrect}'")
                        android.util.Log.d("ROLEPLAY", "    Correct: '${error.correct}'")
                        android.util.Log.d("ROLEPLAY", "    Explanation: ${error.explanation}")
                    }
                    android.util.Log.d("ROLEPLAY", "  Feedback: ${response.correction.feedback}")
                } else {
                    android.util.Log.d("ROLEPLAY", "✓ No errors - message was correct (hasErrors=false)")
                }
                android.util.Log.d("ROLEPLAY", "Current stage: ${response.currentStage}")
                android.util.Log.d("ROLEPLAY", "Completed: ${response.isCompleted}")

                // Convert correction object to display string
                val correctionText = response.correction?.toDisplayString()

                android.util.Log.d("ROLEPLAY", "--- DISPLAY STRING CONVERSION ---")
                if (correctionText != null) {
                    android.util.Log.d("ROLEPLAY", "✅ Correction display text: '$correctionText'")
                } else {
                    android.util.Log.d("ROLEPLAY", "❌ toDisplayString() returned NULL - correction will NOT be shown!")
                    android.util.Log.d("ROLEPLAY", "This means the correction format doesn't match any case in toDisplayString()")
                }
                android.util.Log.d("ROLEPLAY", "═══════════════════════════════════════")

                // Update with response
                _state.update { s ->
                    val updated = s.messages.toMutableList()
                    val lastIdx = updated.indexOfLast { it.role == "assistant" && it.streaming }
                    if (lastIdx >= 0) {
                        updated[lastIdx] = updated[lastIdx].copy(
                            text = response.aiMessage,
                            correction = correctionText,
                            streaming = false
                        )
                    }
                    lastTurnCompletedFlag = response.isCompleted
                    s.copy(
                        messages = updated,
                        sending = false,
                        currentStage = response.currentStage,
                        stageDescription = null
                    )
                }

                // If roleplay is completed, optionally speak completion message
                if (response.isCompleted) {
                    kotlinx.coroutines.delay(1000)
                }

            } catch (t: Throwable) {
                android.util.Log.e("ROLEPLAY", "═══════════════════════════════════════")
                android.util.Log.e("ROLEPLAY", "❌ ROLEPLAY TURN FAILED")
                android.util.Log.e("ROLEPLAY", "Error type: ${t.javaClass.simpleName}")
                android.util.Log.e("ROLEPLAY", "Error message: ${t.message}")
                android.util.Log.e("ROLEPLAY", "Stack trace:", t)
                android.util.Log.e("ROLEPLAY", "═══════════════════════════════════════")

                val errorMsg = when {
                    t.message?.contains("404") == true ->
                        "Server error: /roleplay/turn endpoint not found. Check server."
                    t.message?.contains("500") == true ->
                        "Server error (500). Check server logs for Azure configuration or roleplay_referee.py errors."
                    t.message?.contains("Connection refused") == true || t.message?.contains("failed to connect") == true ->
                        "Cannot connect to server. Check server is running and network config."
                    t.message?.contains("offline") == true ->
                        "Server offline. Update ngrok URL or start server."
                    else -> "Error: ${t.message}"
                }

                _state.update { s ->
                    val updated = s.messages.toMutableList()
                    val lastIdx = updated.indexOfLast { it.role == "assistant" && it.streaming }
                    if (lastIdx >= 0) {
                        updated[lastIdx] = updated[lastIdx].copy(
                            text = errorMsg,
                            streaming = false
                        )
                    }
                    s.copy(messages = updated, sending = false, error = errorMsg)
                }
            }
        }
    }

    fun onMicTapped() {
        if (state.value.recording) stopRecording() else startRecording()
    }

    private fun startRecording() {
        _state.update { it.copy(recording = true, error = null, input = "") }
        stt.start(
            onPartial = { partial ->
                android.util.Log.d("ROLEPLAY_STT", "Partial: $partial")
                _state.update { it.copy(input = partial) }
            },
            onFinal = { final ->
                android.util.Log.d("ROLEPLAY_STT", "Final: $final")
                _state.update { it.copy(recording = false, input = "") }
                if (final.isNotBlank() && state.value.sessionStarted) {
                    sendInternal(final)
                }
            },
            onError = { e ->
                android.util.Log.e("ROLEPLAY_STT", "Error: $e")
                _state.update { it.copy(recording = false, error = e, input = "") }
            }
        )
    }

    private fun stopRecording() {
        android.util.Log.d("ROLEPLAY_STT", "Manually stopping recording")
        stt.stop()
        _state.update { it.copy(recording = false, input = "") }
    }

    fun speakMessage(text: String) {
        // Stop any ongoing TTS to prevent echo/overlap
        tts.stop()
        tts.speak(text)
    }

    fun stopTts() {
        tts.stop()
    }

    private fun gen() = System.nanoTime().toString()
}
