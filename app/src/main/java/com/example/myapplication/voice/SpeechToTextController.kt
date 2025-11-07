package com.example.myapplication.voice

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

open class SpeechToTextController(private val app: Application) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var onPartialCallback: ((String) -> Unit)? = null
    private var onFinalCallback: ((String) -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null
    private var lastPartialResult = ""

    open fun start(onPartial: (String) -> Unit, onFinal: (String) -> Unit, onError: (String) -> Unit) {
        onPartialCallback = onPartial
        onFinalCallback = onFinal
        onErrorCallback = onError
        lastPartialResult = ""

        if (!SpeechRecognizer.isRecognitionAvailable(app)) {
            onError("Speech recognition not available")
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(app).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d("STT", "Ready for speech")
                }

                override fun onBeginningOfSpeech() {
                    Log.d("STT", "Speech started")
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Could show volume indicator here
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    Log.d("STT", "Speech ended")
                }

                override fun onError(error: Int) {
                    when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> {
                            // If we got partial results, use those
                            if (lastPartialResult.isNotBlank()) {
                                Log.d("STT", "No match but using partial: $lastPartialResult")
                                onFinalCallback?.invoke(lastPartialResult)
                            } else {
                                Log.w("STT", "No speech detected, try speaking louder or closer to mic")
                                onErrorCallback?.invoke("No speech detected - try again")
                            }
                        }
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                            if (lastPartialResult.isNotBlank()) {
                                Log.d("STT", "Timeout but using partial: $lastPartialResult")
                                onFinalCallback?.invoke(lastPartialResult)
                            } else {
                                Log.w("STT", "No speech input detected")
                                onErrorCallback?.invoke("No speech heard - tap mic to try again")
                            }
                        }
                        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                            Log.e("STT", "Network error - speech recognition requires internet")
                            onErrorCallback?.invoke("Network error - check internet connection")
                        }
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                            Log.e("STT", "Microphone permission denied")
                            onErrorCallback?.invoke("Microphone permission required")
                        }
                        else -> {
                            val msg = when (error) {
                                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                                SpeechRecognizer.ERROR_CLIENT -> "Client error"
                                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                                SpeechRecognizer.ERROR_SERVER -> "Server error"
                                else -> "Error code: $error"
                            }
                            Log.e("STT", "Error: $msg")
                            onErrorCallback?.invoke(msg)
                        }
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: lastPartialResult
                    Log.d("STT", "Final result: $text")
                    if (text.isNotBlank()) {
                        onFinalCallback?.invoke(text)
                    } else {
                        onErrorCallback?.invoke("No speech recognized")
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    if (text.isNotBlank()) {
                        lastPartialResult = text
                        Log.d("STT", "Partial result: $text")
                        onPartialCallback?.invoke(text)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, app.packageName)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 3000L)
        }

        speechRecognizer?.startListening(intent)
        Log.d("STT", "Started listening...")
    }

    open fun stop() {
        Log.d("STT", "Stopping recognition")
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        onPartialCallback = null
        onFinalCallback = null
        onErrorCallback = null
        lastPartialResult = ""
    }
}
