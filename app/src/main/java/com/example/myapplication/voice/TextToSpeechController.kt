package com.example.myapplication.voice

import android.app.Application
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

open class TextToSpeechController(private val app: Application) {

    protected fun <T : Application> getApplication(): T {
        @Suppress("UNCHECKED_CAST")
        return app as T
    }
    private var tts: TextToSpeech? = null
    private var initialized = false

    init {
        tts = TextToSpeech(app) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
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
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        Log.d("TTS", "Speaking: $text")
    }

    open fun stop() {
        tts?.stop()
    }

    open fun shutdown() {
        tts?.shutdown()
        tts = null
        initialized = false
    }
}
