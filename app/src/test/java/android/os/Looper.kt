package android.os

object Looper {
    private val mainLooper = Looper
    @JvmStatic fun getMainLooper(): Looper = mainLooper
    @JvmStatic fun myLooper(): Looper? = mainLooper
    fun prepare() { /* no-op for tests */ }
    fun loop() { /* no-op */ }
}
