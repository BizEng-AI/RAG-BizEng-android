package android.os

object Looper {
    private val mainLooper = Looper

    fun getMainLooper(): Looper = mainLooper
    fun prepare() { /* no-op for tests */ }
    fun loop() { /* no-op */ }
}

