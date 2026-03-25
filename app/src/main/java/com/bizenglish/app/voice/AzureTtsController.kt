package com.bizenglish.app.voice

import android.app.Application
import android.util.Log
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import android.media.MediaPlayer

/**
 * Azure Neural Text-to-Speech Controller
 * Uses Azure Cognitive Services for high-quality, natural-sounding speech
 *
 * Free Tier: 5 million characters per month (Neural voices)
 * Quality: Significantly better than Android TTS - sounds human-like
 */
class AzureTtsController(app: Application) : TextToSpeechController(app) {

    // Azure Speech Service Configuration — set via BuildConfig (build.gradle.kts)
    private val AZURE_SPEECH_KEY = com.bizenglish.app.BuildConfig.AZURE_SPEECH_KEY
    private val AZURE_SPEECH_REGION = com.bizenglish.app.BuildConfig.AZURE_SPEECH_REGION

    // Neural voice selection (sounds most natural)
    // en-US-JennyNeural = Female, professional, clear
    // en-US-GuyNeural = Male, professional
    // en-US-AriaNeural = Female, conversational, warm
    private val VOICE_NAME = "en-US-JennyNeural"

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Speak text using Azure Neural TTS
     * Automatically downloads audio and plays it
     */
    override fun speak(text: String) {
        if (text.isBlank()) return

        Log.d("AZURE_TTS", "Speaking: $text")

        // Stop any currently playing audio
        stop()

        scope.launch {
            try {
                // Generate audio from Azure
                val audioFile = synthesizeSpeech(text)

                // Play the audio
                withContext(Dispatchers.Main) {
                    playAudio(audioFile)
                }

            } catch (e: Exception) {
                Log.e("AZURE_TTS", "Failed to speak", e)
                withContext(Dispatchers.Main) {
                    // Fallback to Android TTS if Azure fails
                    Log.w("AZURE_TTS", "Falling back to Android TTS")
                    // Could add fallback here if needed
                }
            }
        }
    }

    /**
     * Call Azure Speech Service to synthesize speech
     * Returns a temporary audio file
     */
    private suspend fun synthesizeSpeech(text: String): File = withContext(Dispatchers.IO) {
        Log.d("AZURE_TTS", "Requesting speech synthesis from Azure...")

        // Create SSML (Speech Synthesis Markup Language) for better control
        val ssml = """
            <speak version='1.0' xml:lang='en-US'>
                <voice name='$VOICE_NAME'>
                    <prosody rate='0.95' pitch='+0%'>
                        $text
                    </prosody>
                </voice>
            </speak>
        """.trimIndent()

        // Azure TTS endpoint
        val endpoint = "https://$AZURE_SPEECH_REGION.tts.speech.microsoft.com/cognitiveservices/v1"

        val url = URL(endpoint)
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Ocp-Apim-Subscription-Key", AZURE_SPEECH_KEY)
            connection.setRequestProperty("Content-Type", "application/ssml+xml")
            connection.setRequestProperty("X-Microsoft-OutputFormat", "audio-16khz-128kbitrate-mono-mp3")
            connection.setRequestProperty("User-Agent", "BizEnglishApp")
            connection.doOutput = true

            // Send SSML request
            connection.outputStream.use { it.write(ssml.toByteArray()) }

            val responseCode = connection.responseCode
            Log.d("AZURE_TTS", "Azure response code: $responseCode")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Save audio to temp file
                val tempFile = File.createTempFile("azure_tts_", ".mp3", getApplication<Application>().cacheDir)

                connection.inputStream.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }

                Log.d("AZURE_TTS", "✓ Audio downloaded: ${tempFile.length()} bytes")
                return@withContext tempFile

            } else {
                val error = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                throw Exception("Azure TTS failed: $responseCode - $error")
            }

        } finally {
            connection.disconnect()
        }
    }

    /**
     * Play the audio file using MediaPlayer
     */
    private fun playAudio(audioFile: File) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFile.absolutePath)
                setOnCompletionListener {
                    // Clean up after playback
                    release()
                    audioFile.delete()
                    Log.d("AZURE_TTS", "Playback completed")
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("AZURE_TTS", "MediaPlayer error: what=$what, extra=$extra")
                    release()
                    audioFile.delete()
                    true
                }
                prepare()
                start()
            }

            Log.d("AZURE_TTS", "Playing audio...")

        } catch (e: Exception) {
            Log.e("AZURE_TTS", "Failed to play audio", e)
            audioFile.delete()
        }
    }

    /**
     * Stop currently playing audio
     */
    override fun stop() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null
            Log.d("AZURE_TTS", "Stopped playback")
        } catch (e: Exception) {
            Log.e("AZURE_TTS", "Error stopping playback", e)
        }
    }

    /**
     * Cleanup resources
     */
    override fun shutdown() {
        stop()
        scope.cancel()
        Log.d("AZURE_TTS", "Shutdown complete")
    }
}

