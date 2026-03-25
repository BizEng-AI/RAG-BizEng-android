package com.bizenglish.app.data.remote.voice
import android.media.MediaPlayer
import android.media.MediaRecorder
import java.io.File

class VoiceRecorder {
    private var recorder: MediaRecorder? = null
    fun start(outFile: File) {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(96000)
            setAudioSamplingRate(44100)
            setOutputFile(outFile.absolutePath)
            prepare()
            start()
        }
    }
    fun stop() { runCatching { recorder?.stop() }; recorder?.release(); recorder = null }
}

class VoicePlayer {
    private val player = MediaPlayer()
    fun play(bytes: ByteArray) {
        player.reset()
        val tmp = File.createTempFile("tts",".mp3")
        tmp.writeBytes(bytes)
        player.setDataSource(tmp.absolutePath)
        player.prepare()
        player.start()
    }
}
