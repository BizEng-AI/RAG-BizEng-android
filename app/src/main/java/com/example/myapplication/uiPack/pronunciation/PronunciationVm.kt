package com.example.myapplication.uiPack.pronunciation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.myapplication.domain.repository.RagRepository
import com.example.myapplication.voice.SpeechToTextController
import com.example.myapplication.voice.TextToSpeechController
import com.example.myapplication.voice.AudioRecorder
import java.io.File

import com.example.myapplication.data.remote.dto.PronunciationResultDto

data class PronunciationUiState(
    val inputText: String = "",
    val targetPhrase: String = "",
    val recording: Boolean = false,
    val assessing: Boolean = false,
    val error: String? = null,
    val result: PronunciationResultDto? = null,  // Use DTO directly - it has all the new IPA fields
    val showExampleMode: Boolean = true  // true = type & hear, false = record & assess
)


@HiltViewModel
class PronunciationVm @Inject constructor(
    app: Application,
    private val repo: RagRepository,
    private val stt: SpeechToTextController,
    private val tts: TextToSpeechController
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(PronunciationUiState())

    private val audioRecorder = AudioRecorder()
    private var currentRecordingFile: File? = null
    val state: StateFlow<PronunciationUiState> = _state.asStateFlow()

    // Common business English phrases for practice
    val suggestedPhrases = listOf(
        "Good morning, I would like to schedule a meeting",
        "Could you please send me the quarterly report?",
        "Let's discuss the project timeline",
        "I appreciate your prompt response",
        "We need to address this issue immediately",
        "Thank you for your cooperation",
        "I look forward to hearing from you",
        "Please find the attached document",
        "I would like to follow up on our conversation",
        "Could we reschedule our appointment?"
    )

    fun onInputChange(text: String) {
        _state.update { it.copy(inputText = text, error = null) }
    }

    fun setTargetPhrase(phrase: String) {
        _state.update { it.copy(targetPhrase = phrase, inputText = phrase, error = null, result = null) }
    }

    fun speakPhrase() {
        val phrase = state.value.targetPhrase.ifBlank { state.value.inputText }
        if (phrase.isBlank()) {
            _state.update { it.copy(error = "Please enter a word or phrase first") }
            return
        }
        tts.speak(phrase)
        android.util.Log.d("PRONUNCIATION", "Speaking: $phrase")
    }

    fun onPracticeButtonClicked() {
        val text = state.value.inputText.trim()
        if (text.isBlank()) {
            _state.update { it.copy(error = "Please enter a word or phrase to practice") }
            return
        }
        _state.update { it.copy(targetPhrase = text, showExampleMode = false, error = null, result = null) }
    }

    fun onMicTapped() {
        if (state.value.recording) stopRecording() else startRecording()
    }

    private fun startRecording() {
        if (state.value.targetPhrase.isBlank()) {
            _state.update { it.copy(error = "Please set a target phrase first") }
            return
        }

        android.util.Log.d("PRONUNCIATION", "═══════════════════════════════════════")
        android.util.Log.d("PRONUNCIATION", "STARTING PRONUNCIATION RECORDING")
        android.util.Log.d("PRONUNCIATION", "Target phrase: ${state.value.targetPhrase}")

        // Create WAV file in cache directory
        val cacheDir = getApplication<Application>().cacheDir
        cacheDir.mkdirs() // Ensure cache directory exists

        val tempFile = File(cacheDir, "pronunciation_${System.currentTimeMillis()}.wav")
        currentRecordingFile = tempFile

        android.util.Log.d("PRONUNCIATION", "Recording to: ${tempFile.absolutePath}")
        android.util.Log.d("PRONUNCIATION", "Cache dir exists: ${cacheDir.exists()}")
        android.util.Log.d("PRONUNCIATION", "Cache dir writable: ${cacheDir.canWrite()}")

        _state.update { it.copy(recording = true, error = null, result = null) }

        audioRecorder.startRecording(tempFile) { error ->
            android.util.Log.e("PRONUNCIATION", "❌ Recording error: $error")
            _state.update { it.copy(recording = false, error = "Recording error: $error") }
        }

        android.util.Log.d("PRONUNCIATION", "✓ Audio recording started")
    }

    private fun stopRecording() {
        android.util.Log.d("PRONUNCIATION", "Stopping recording...")

        val stopped = audioRecorder.stopRecording()
        _state.update { it.copy(recording = false) }

        if (!stopped) {
            _state.update { it.copy(error = "Failed to stop recording") }
            return
        }

        val file = currentRecordingFile
        if (file == null || !file.exists() || file.length() == 0L) {
            android.util.Log.e("PRONUNCIATION", "❌ Recording file invalid or empty")
            _state.update { it.copy(error = "Recording failed - no audio captured") }
            return
        }

        android.util.Log.d("PRONUNCIATION", "✓ Recording saved: ${file.length()} bytes")
        assessRecording(file)
    }

    private fun assessRecording(audioFile: File) {
        viewModelScope.launch {
            _state.update { it.copy(assessing = true, error = null) }

            try {
                android.util.Log.d("PRONUNCIATION", "═══════════════════════════════════════")
                android.util.Log.d("PRONUNCIATION", "STARTING PRONUNCIATION ASSESSMENT")
                android.util.Log.d("PRONUNCIATION", "Reference text: ${state.value.targetPhrase}")
                android.util.Log.d("PRONUNCIATION", "Audio file: ${audioFile.absolutePath}")
                android.util.Log.d("PRONUNCIATION", "File exists: ${audioFile.exists()}")
                android.util.Log.d("PRONUNCIATION", "File size: ${audioFile.length()} bytes")

                if (!audioFile.exists()) {
                    throw IllegalStateException("Audio file not found: ${audioFile.absolutePath}")
                }

                if (audioFile.length() < 1000) {
                    throw IllegalStateException("Audio file too small (${audioFile.length()} bytes) - recording may have failed")
                }

                // Call server pronunciation assessment endpoint
                val result = repo.assessPronunciation(audioFile, state.value.targetPhrase)

                android.util.Log.d("PRONUNCIATION", "═══════════════════════════════════════")
                android.util.Log.d("PRONUNCIATION", "✓ ASSESSMENT SUCCESSFUL")
                android.util.Log.d("PRONUNCIATION", "Transcript: ${result.transcript}")
                android.util.Log.d("PRONUNCIATION", "Accuracy: ${result.accuracyScore}/100")
                android.util.Log.d("PRONUNCIATION", "Fluency: ${result.fluencyScore}/100")
                android.util.Log.d("PRONUNCIATION", "Completeness: ${result.completenessScore}/100")
                android.util.Log.d("PRONUNCIATION", "Pronunciation Score: ${result.pronunciationScore}/100")
                android.util.Log.d("PRONUNCIATION", "Words with IPA: ${result.words.count { it.ipaExpected != null }}")
                android.util.Log.d("PRONUNCIATION", "═══════════════════════════════════════")

                // Use the DTO directly - it already has all the enhanced fields (IPA, phonemes, etc.)
                _state.update { it.copy(assessing = false, result = result) }

                // Clean up temp file
                try {
                    if (audioFile.exists()) {
                        audioFile.delete()
                        android.util.Log.d("PRONUNCIATION", "Cleaned up temp file")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("PRONUNCIATION", "Failed to delete temp file", e)
                }

            } catch (e: Exception) {
                android.util.Log.e("PRONUNCIATION", "═══════════════════════════════════════")
                android.util.Log.e("PRONUNCIATION", "❌ ASSESSMENT FAILED")
                android.util.Log.e("PRONUNCIATION", "Error type: ${e.javaClass.simpleName}")
                android.util.Log.e("PRONUNCIATION", "Error message: ${e.message}")
                android.util.Log.e("PRONUNCIATION", "Stack trace:", e)
                android.util.Log.e("PRONUNCIATION", "═══════════════════════════════════════")

                val errorMsg = when {
                    e.message?.contains("404") == true -> "Pronunciation service not available. Check server is running."
                    e.message?.contains("500") == true -> "Server error. Check server logs for Azure configuration."
                    e.message?.contains("Connection refused") == true -> "Cannot connect to server. Check network."
                    e.message?.contains("offline") == true -> "Server is offline. Update ngrok URL or start server."
                    e.message?.contains("ENOENT") == true -> "File error: ${e.message}"
                    else -> "Error: ${e.message}"
                }
                _state.update { it.copy(assessing = false, error = errorMsg) }
            }
        }
    }


    fun resetToExampleMode() {
        _state.update {
            PronunciationUiState(
                inputText = it.inputText,
                targetPhrase = it.targetPhrase,
                showExampleMode = true
            )
        }
    }

    fun stopTts() {
        tts.stop()
    }
}

