package com.example.myapplication.tracking

import android.app.Application
import com.example.myapplication.voice.SpeechToTextController
import com.example.myapplication.voice.TextToSpeechController
import com.example.myapplication.TestApplication

/**
 * Fake STT that does nothing (for JVM unit tests). Does NOT call super() to avoid Android handlers.
 */
class FakeSpeechToTextController : SpeechToTextController {
    // Use a lightweight test Application so the non-null constructor parameter is satisfied
    constructor() : super(TestApplication())

    override fun start(onPartial: (String) -> Unit, onFinal: (String) -> Unit, onError: (String) -> Unit) {
        // no-op
    }

    override fun stop() {
        // no-op
    }
}

/**
 * Fake TTS that does nothing (for JVM unit tests). Does NOT call super() to avoid Android handlers.
 */
class FakeTextToSpeechController : TextToSpeechController {
    // Use a lightweight test Application so the non-null constructor parameter is satisfied
    constructor() : super(TestApplication())

    override fun speak(text: String) {
        // no-op
    }

    override fun isSpeaking(): Boolean = false

    override fun stop() {
        // no-op
    }

    override fun shutdown() {
        // no-op
    }
}
