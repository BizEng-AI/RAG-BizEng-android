package com.example.myapplication.voice

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Records audio to WAV file format for pronunciation assessment
 */
class AudioRecorder {
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingJob: Job? = null

    private val sampleRate = 16000 // Azure Speech Service prefers 16kHz
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) * 2

    fun startRecording(outputFile: File, onError: (String) -> Unit) {
        if (isRecording) {
            Log.w("AudioRecorder", "Already recording")
            return
        }

        try {
            // Ensure parent directory exists
            outputFile.parentFile?.mkdirs()

            Log.d("AudioRecorder", "Starting recording to: ${outputFile.absolutePath}")
            Log.d("AudioRecorder", "Sample rate: $sampleRate, Buffer size: $bufferSize")

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                onError("Failed to initialize audio recorder")
                return
            }

            isRecording = true
            audioRecord?.startRecording()

            // Write audio data to file in background
            recordingJob = CoroutineScope(Dispatchers.IO).launch {
                writeAudioDataToFile(outputFile)
            }

            Log.d("AudioRecorder", "✓ Recording started successfully")

        } catch (e: Exception) {
            Log.e("AudioRecorder", "Failed to start recording", e)
            onError("Failed to start recording: ${e.message}")
            cleanup()
        }
    }

    private fun writeAudioDataToFile(outputFile: File) {
        val buffer = ByteArray(bufferSize)
        val tempPcmFile = File(outputFile.parent, "${outputFile.nameWithoutExtension}_temp.pcm")

        try {
            FileOutputStream(tempPcmFile).use { fos ->
                var totalBytes = 0
                while (isRecording) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        fos.write(buffer, 0, read)
                        totalBytes += read
                    }
                }
                Log.d("AudioRecorder", "Recorded $totalBytes bytes of PCM audio")
            }

            // Convert PCM to WAV format
            convertPcmToWav(tempPcmFile, outputFile)
            tempPcmFile.delete()

        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error writing audio data", e)
            tempPcmFile.delete()
        }
    }

    fun stopRecording(): Boolean {
        if (!isRecording) {
            Log.w("AudioRecorder", "Not recording")
            return false
        }

        Log.d("AudioRecorder", "Stopping recording...")
        isRecording = false

        // Wait for recording job to finish
        runBlocking {
            recordingJob?.join()
        }

        cleanup()
        Log.d("AudioRecorder", "✓ Recording stopped")
        return true
    }

    private fun cleanup() {
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            recordingJob?.cancel()
            recordingJob = null
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error during cleanup", e)
        }
    }

    private fun convertPcmToWav(pcmFile: File, wavFile: File) {
        if (!pcmFile.exists()) {
            Log.e("AudioRecorder", "PCM file not found: ${pcmFile.absolutePath}")
            return
        }

        val pcmData = pcmFile.readBytes()
        val totalDataLen = pcmData.size + 36
        val channels = 1
        val byteRate = sampleRate * channels * 2 // 16-bit = 2 bytes

        try {
            FileOutputStream(wavFile).use { fos ->
                // WAV header
                fos.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).put("RIFF".toByteArray()).array())
                fos.write(intToByteArray(totalDataLen))
                fos.write("WAVE".toByteArray())

                // fmt chunk
                fos.write("fmt ".toByteArray())
                fos.write(intToByteArray(16)) // fmt chunk size
                fos.write(shortToByteArray(1)) // audio format (1 = PCM)
                fos.write(shortToByteArray(channels.toShort()))
                fos.write(intToByteArray(sampleRate))
                fos.write(intToByteArray(byteRate))
                fos.write(shortToByteArray((channels * 2).toShort())) // block align
                fos.write(shortToByteArray(16)) // bits per sample

                // data chunk
                fos.write("data".toByteArray())
                fos.write(intToByteArray(pcmData.size))
                fos.write(pcmData)
            }

            Log.d("AudioRecorder", "✓ Converted PCM to WAV: ${wavFile.length()} bytes")
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Failed to convert PCM to WAV", e)
        }
    }

    private fun intToByteArray(value: Int): ByteArray {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()
    }

    private fun shortToByteArray(value: Short): ByteArray {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array()
    }

    fun isCurrentlyRecording(): Boolean = isRecording
}

