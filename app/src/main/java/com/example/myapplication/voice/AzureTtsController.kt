package com.example.myapplication.voice

import android.app.Application
import android.util.Log
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import android.media.MediaPlayer
import java.util.concurrent.atomic.AtomicInteger

/**
 * Azure Neural Text-to-Speech Controller
 * Uses Azure Cognitive Services for high-quality, natural-sounding speech
 */
class AzureTtsController(app: Application) : TextToSpeechController(app) {

    // Azure Speech Service Configuration
    private val AZURE_SPEECH_KEY = "CbZ50wqN8vOc9BwwgUZak4sKkHqtUZSjj31bayNGIVaIn47214zRJQQJ99BJAC3pKaRXJ3w3AAAYACOGKoCE"
    private val AZURE_SPEECH_REGION = "eastasia"

    private val VOICE_NAME = "en-US-JennyNeural"

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Concurrency + cancellation controls
    private var currentJob: Job? = null
    private val speakToken = AtomicInteger(0)
    @Volatile private var currentAudioFile: File? = null

    override fun speak(text: String) {
        if (text.isBlank()) return
        Log.d("AZURE_TTS", "Speaking: $text")

        // Invalidate any in-flight synthesis/playback and stop audio
        stop()

        val myToken = speakToken.incrementAndGet()
        currentJob?.cancel()
        currentJob = scope.launch {
            try {
                val audioFile = synthesizeSpeech(text)

                // If another speak() was requested since we started, discard this file
                if (myToken != speakToken.get()) {
                    Log.d("AZURE_TTS", "Discarding outdated audio file")
                    runCatching { audioFile.delete() }
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    // Double-check before playing
                    if (myToken == speakToken.get()) {
                        playAudio(audioFile)
                    } else {
                        runCatching { audioFile.delete() }
                    }
                }

            } catch (e: CancellationException) {
                Log.d("AZURE_TTS", "Synthesis cancelled")
            } catch (e: Exception) {
                Log.e("AZURE_TTS", "Azure TTS failed, falling back to Android TTS", e)
                // Fallback to Android TTS
                withContext(Dispatchers.Main) {
                    try {
                        super.speak(text)
                        Log.d("AZURE_TTS", "✓ Fallback to Android TTS successful")
                    } catch (fallbackError: Exception) {
                        Log.e("AZURE_TTS", "❌ Both Azure and Android TTS failed!", fallbackError)
                    }
                }
            }
        }
    }

    private suspend fun synthesizeSpeech(text: String): File = withContext(Dispatchers.IO) {
        Log.d("AZURE_TTS", "Requesting speech synthesis from Azure...")

        val ssml = """
            <speak version='1.0' xml:lang='en-US'>
                <voice name='$VOICE_NAME'>
                    <prosody rate='0.95' pitch='+0%'>
                        $text
                    </prosody>
                </voice>
            </speak>
        """.trimIndent()

        val endpoint = "https://${AZURE_SPEECH_REGION}.tts.speech.microsoft.com/cognitiveservices/v1"
        val url = URL(endpoint)
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Ocp-Apim-Subscription-Key", AZURE_SPEECH_KEY)
            connection.setRequestProperty("Content-Type", "application/ssml+xml")
            connection.setRequestProperty("X-Microsoft-OutputFormat", "audio-16khz-128kbitrate-mono-mp3")
            connection.setRequestProperty("User-Agent", "BizEnglishApp")
            connection.doOutput = true

            connection.outputStream.use { it.write(ssml.toByteArray()) }

            val responseCode = connection.responseCode
            Log.d("AZURE_TTS", "Azure response code: $responseCode")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val tempFile = File.createTempFile("azure_tts_", ".mp3", getApplication<Application>().cacheDir)
                connection.inputStream.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d("AZURE_TTS", "✓ Audio downloaded: ${'$'}{tempFile.length()} bytes")
                return@withContext tempFile
            } else {
                val error = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                throw Exception("Azure TTS failed: ${'$'}responseCode - ${'$'}error")
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun playAudio(audioFile: File) {
        try {
            // Always stop existing playback first
            mediaPlayer?.apply {
                runCatching { if (isPlaying) stop() }
                runCatching { release() }
            }
            mediaPlayer = null

            currentAudioFile?.let { runCatching { if (it.exists()) it.delete() } }
            currentAudioFile = audioFile

            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFile.absolutePath)
                setOnCompletionListener {
                    runCatching { it.release() }
                    runCatching { audioFile.delete() }
                    mediaPlayer = null
                    currentAudioFile = null
                    Log.d("AZURE_TTS", "Playback completed")
                }
                setOnErrorListener { mp, what, extra ->
                    Log.e("AZURE_TTS", "MediaPlayer error: what=${'$'}what, extra=${'$'}extra")
                    runCatching { mp.release() }
                    runCatching { audioFile.delete() }
                    mediaPlayer = null
                    currentAudioFile = null
                    true
                }
                prepare()
                start()
            }

            Log.d("AZURE_TTS", "Playing audio...")

        } catch (e: Exception) {
            Log.e("AZURE_TTS", "Failed to play audio", e)
            runCatching { audioFile.delete() }
            mediaPlayer = null
            currentAudioFile = null
        }
    }

    override fun stop() {
        // Invalidate any in-flight speak (discard upcoming playback)
        speakToken.incrementAndGet()

        // Cancel synthesis job
        currentJob?.cancel()
        currentJob = null

        // Stop and release player
        try {
            mediaPlayer?.apply {
                runCatching { if (isPlaying) stop() }
                runCatching { release() }
            }
        } catch (e: Exception) {
            Log.e("AZURE_TTS", "Error stopping playback", e)
        } finally {
            mediaPlayer = null
        }

        // Delete any pending audio file
        currentAudioFile?.let { file ->
            runCatching { if (file.exists()) file.delete() }
        }
        currentAudioFile = null

        Log.d("AZURE_TTS", "Stopped playback & cancelled synthesis")
    }

    override fun shutdown() {
        stop()
        scope.cancel()
        Log.d("AZURE_TTS", "Shutdown complete")
    }
}
