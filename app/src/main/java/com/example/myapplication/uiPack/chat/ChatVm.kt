package com.example.myapplication.uiPack.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.myapplication.data.remote.dto.ChatMsgDto
import com.example.myapplication.domain.repository.RagRepository
import com.example.myapplication.voice.SpeechToTextController
import com.example.myapplication.voice.TextToSpeechController
import com.example.myapplication.data.repository.TrackingRepository
import com.example.myapplication.di.CoroutinesModule.IODispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException
import com.example.myapplication.ui.common.UiErrorMapper
import com.example.myapplication.ui.common.MicState

data class UiMsg(
    val id: String,
    val role: String,      // "user" | "assistant"
    val text: String,
    val streaming: Boolean = false
)

data class ChatUiState(
    val messages: List<UiMsg> = emptyList(),
    val input: String = "",
    val grounded: Boolean = true,
    val micState: MicState = MicState.Idle,
    val sending: Boolean = false,
    val error: String? = null,
    val attemptTrackingId: String? = null // tracking attempt id
)

@HiltViewModel
class ChatVm @Inject constructor(
     private val repo: RagRepository,
     private val stt: SpeechToTextController,
     private val tts: TextToSpeechController,
     private val trackingRepository: TrackingRepository,
     @IODispatcher private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private var attemptCompleted = false
    private var micPreSpeechInput: String = ""

    fun onInputChange(v: String) = _state.update { it.copy(input = v, error = null) }
    fun toggleGrounding() = _state.update { it.copy(grounded = !it.grounded) }

    fun send() {
        val text = state.value.input.trim()
        if (text.isEmpty() || state.value.sending) return
        _state.update { it.copy(input = "") }
        sendInternal(text)
    }

    private fun mapChatError(t: Throwable): String = UiErrorMapper.mapChatError(t)

    private fun sendInternal(text: String) {
        viewModelScope.launch(dispatcher) {
            // Start exercise attempt if first message
            if (state.value.attemptTrackingId == null) {
                val attemptRes = trackingRepository.startExercise("chat_session", "chat")
                val id = attemptRes.getOrNull()?.id
                if (id != null) _state.update { it.copy(attemptTrackingId = id) }
            }

            android.util.Log.d("CHAT", "═══════════════════════════════════════")
            android.util.Log.d("CHAT", "SENDING CHAT MESSAGE")
            android.util.Log.d("CHAT", "User message: $text")
            android.util.Log.d("CHAT", "Grounded mode: ${state.value.grounded}")

            _state.update {
                it.copy(
                    sending = true,
                    messages = it.messages + UiMsg(id = gen(), role = "user", text = text) +
                            UiMsg(id = gen(), role = "assistant", text = "", streaming = true)
                )
            }
            try {
                val grounded = state.value.grounded
                val assistantText = if (grounded) {
                    android.util.Log.d("CHAT", "Using grounded (RAG) mode - calling /ask endpoint")
                    val result = repo.askGrounded(text)
                    android.util.Log.d("CHAT", "✓ Grounded response received: ${result.answer.take(100)}...")
                    result.answer
                } else {
                    android.util.Log.d("CHAT", "Using free chat mode - calling /chat endpoint")
                    val msgs = state.value.messages
                        .filter { it.role == "user" || it.role == "assistant" }
                        .map { ChatMsgDto(role = it.role, content = it.text) } +
                            ChatMsgDto(role = "user", content = text)

                    android.util.Log.d("CHAT", "═══ MESSAGES BEING SENT TO SERVER ═══")
                    android.util.Log.d("CHAT", "Total messages: ${msgs.size}")
                    msgs.forEachIndexed { idx, msg ->
                        android.util.Log.d("CHAT", "  Message[$idx]: role='${msg.role}' content='${msg.content.take(50)}${if(msg.content.length > 50) "..." else ""}'")
                    }
                    android.util.Log.d("CHAT", "═══════════════════════════════════")

                    val result = repo.chatFree(msgs)
                    val responseText = result.getResponseText()
                    android.util.Log.d("CHAT", "✓ Free chat response received: ${responseText.take(100)}...")
                    responseText
                }

                android.util.Log.d("CHAT", "✓ Chat successful")
                android.util.Log.d("CHAT", "═══════════════════════════════════════")

                _state.update { s ->
                    val updated = s.messages.toMutableList()
                    val lastIdx = updated.indexOfLast { it.role == "assistant" && it.streaming }
                    if (lastIdx >= 0) updated[lastIdx] = updated[lastIdx].copy(text = assistantText, streaming = false)
                    s.copy(messages = updated, sending = false)
                }
            } catch (t: Throwable) {
                android.util.Log.e("CHAT", "══════════════════════════════════════════════════════")
                android.util.Log.e("CHAT", "✖ CHAT FAILED")
                android.util.Log.e("CHAT", "Error type: ${t.javaClass.simpleName}")
                android.util.Log.e("CHAT", "Error message: ${t.message}")
                android.util.Log.e("CHAT", "Stack trace:", t)
                android.util.Log.e("CHAT", "══════════════════════════════════════════════════════")

                val errorMsg = mapChatError(t)
                _state.update { s ->
                    val updated = s.messages.toMutableList()
                    val lastIdx = updated.indexOfLast { it.role == "assistant" && it.streaming }
                    if (lastIdx >= 0) updated[lastIdx] = updated[lastIdx].copy(text = errorMsg, streaming = false)
                    s.copy(messages = updated, sending = false, error = errorMsg)
                }
            }
        }
    }

    fun onMicTapped() {
        when (state.value.micState) {
            MicState.Listening -> stopRecording()
            MicState.Processing -> Unit
            else -> startRecording()
        }
    }

    private fun mergeSpeechWithBase(transcript: String): String {
        val base = micPreSpeechInput
        if (transcript.isBlank()) return base
        if (base.isBlank()) return transcript
        val needsSpace = !base.last().isWhitespace()
        return buildString {
            append(base)
            if (needsSpace) append(' ')
            append(transcript)
        }
    }

    private fun startRecording() {
        micPreSpeechInput = state.value.input.trimEnd()
        _state.update { it.copy(micState = MicState.Listening, error = null) }
        stt.start(
            onPartial = { partial ->
                val normalized = partial.trim()
                val nextInput = if (normalized.isBlank()) micPreSpeechInput else mergeSpeechWithBase(normalized)
                _state.update { it.copy(input = nextInput) }
            },
            onFinal = { final ->
                _state.update { it.copy(micState = MicState.Processing) }
                val normalized = final.trim()
                val nextInput = if (normalized.isBlank()) micPreSpeechInput else mergeSpeechWithBase(normalized)
                micPreSpeechInput = ""
                _state.update { it.copy(input = nextInput, micState = MicState.Idle) }
            },
            onError = { e ->
                val sanitized = com.example.myapplication.ui.common.UiErrorMapper.mapChatError(Throwable(e))
                micPreSpeechInput = ""
                _state.update { it.copy(micState = MicState.Error, error = sanitized) }
            }
        )
    }

    private fun stopRecording() {
        android.util.Log.d("STT_VM", "Manually stopping recording")
        stt.stop()
        micPreSpeechInput = ""
        _state.update { it.copy(micState = MicState.Idle) }
    }

    fun speakMessage(text: String) {
        // Stop any ongoing TTS to prevent echo/overlap
        tts.stop()
        tts.speak(text)
    }

    fun stopTts() {
        tts.stop()
        // Optional: could trigger completion here, but better on screen dispose
    }

    fun completeAttemptIfNeeded() {
        val id = state.value.attemptTrackingId
        if (id != null && !attemptCompleted) {
            attemptCompleted = true
            viewModelScope.launch(dispatcher) {
                val assistantReplies = state.value.messages.count { it.role == "assistant" && !it.streaming && it.text.isNotBlank() }
                val abandoned = assistantReplies == 0
                if (abandoned) {
                    trackingRepository.abandonExercise(id, "chat_session", "chat", null)
                } else {
                    trackingRepository.updateExercise(
                        attemptId = id,
                        status = "completed",
                        score = null,
                        durationSec = null // auto-compute by repository
                    )
                }
            }
        }
    }

    private fun gen() = System.nanoTime().toString()
}
