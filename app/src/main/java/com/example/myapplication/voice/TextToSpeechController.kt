package com.example.myapplication.voice

import android.app.Application
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

open class TextToSpeechController(private val app: Application) {

    protected fun <T : Application> getApplication(): T {
        @Suppress("UNCHECKED_CAST")
        return app as T
    }

    private var tts: TextToSpeech? = null
    private var initialized = false
    private val speakLock = Any()
    @Volatile private var speaking: Boolean = false

    init {
        tts = TextToSpeech(app) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        speaking = true
                        Log.d("TTS", "onStart: $utteranceId")
                    }
                    override fun onDone(utteranceId: String?) {
                        speaking = false
                        Log.d("TTS", "onDone: $utteranceId")
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        speaking = false
                        Log.e("TTS", "onError: $utteranceId")
                    }
                })
                initialized = true
                Log.d("TTS", "TextToSpeech initialized successfully")
            } else {
                Log.e("TTS", "TextToSpeech initialization failed")
            }
        }
    }

    open fun speak(text: String) {
        if (!initialized) {
            Log.w("TTS", "TTS not initialized yet")
            return
        }
        synchronized(speakLock) {
            // Ensure no overlap: stop any ongoing utterance and speak with FLUSH
            try { tts?.stop() } catch (_: Exception) {}
            val utteranceId = "utt_${System.currentTimeMillis()}"
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            Log.d("TTS", "Speaking: $text")
        }
    }

    open fun isSpeaking(): Boolean = speaking

    open fun stop() {
        try { tts?.stop() } catch (_: Exception) {}
        speaking = false
    }

    open fun shutdown() {
        try { tts?.shutdown() } catch (_: Exception) {}
        tts = null
        initialized = false
        speaking = false
    }
}
