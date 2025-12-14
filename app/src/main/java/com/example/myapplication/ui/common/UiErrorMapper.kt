package com.example.myapplication.ui.common

import android.util.Log
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

object GuardrailMetrics {
    private val counts = mutableMapOf<String, Int>()
    @Synchronized fun increment(domain: String, classification: String) {
        counts["$domain:$classification"] = counts.getOrDefault("$domain:$classification", 0) + 1
    }
    @Synchronized fun snapshot(): Map<String, Int> = counts.toMap()
    @Synchronized fun reset() { counts.clear() }
}

/** Centralized, user-friendly error mapping to avoid drift across modules. */
object UiErrorMapper {
    private const val TAG = "UiErrorMapper"

    private fun telemetry(domain: String, raw: String?, classification: String) {
        Log.d(TAG, "guardrail domain=$domain class=$classification raw=$raw")
        GuardrailMetrics.increment(domain, classification)
    }

    private fun isMatch(raw: String, vararg needles: String): Boolean =
        needles.any { raw.contains(it, ignoreCase = true) }

    private fun classify(raw: String): String = when {
        raw.isBlank() -> "blank"
        isMatch(raw.lowercase(), "timeout") -> "timeout"
        isMatch(raw.lowercase(), "connection refused", "failed to connect") -> "connect"
        isMatch(raw.lowercase(), "offline") -> "offline"
        isMatch(raw.lowercase(), "401", "unauthorized") -> "auth"
        isMatch(raw.lowercase(), "403") -> "forbidden"
        isMatch(raw.lowercase(), "content management policy", "content policy", "filtered") -> "content_filter"
        isMatch(raw.lowercase(), "model not found") -> "model_missing"
        isMatch(raw.lowercase(), "404") -> "not_found"
        isMatch(raw.lowercase(), "500", "internal server error") -> "server_error"
        isMatch(raw.lowercase(), "enoent") -> "file"
        else -> "other"
    }

    private fun baseMap(domain: String, t: Throwable?): String {
        val raw = t?.message.orEmpty()
        val msgLower = raw.lowercase()
        val result = when {
            t is UnknownHostException -> "Cannot reach server. Check your internet connection."
            t is SSLHandshakeException -> "Secure connection failed. Please try again later."
            isMatch(msgLower, "timeout") -> "Request timed out. Please try again."
            isMatch(msgLower, "connection refused", "failed to connect") -> "Could not connect to the service. Please try again later."
            isMatch(msgLower, "offline") -> "Service appears offline. Please try again soon."
            isMatch(msgLower, "401", "unauthorized") -> "Session expired. Please log in again."
            isMatch(msgLower, "403") -> "You do not have permission for this action."
            isMatch(msgLower, "content management policy", "content policy", "filtered") -> "This message was blocked. Please rephrase and try again."
            isMatch(msgLower, "model not found") -> "Required model not available. Please try again later."
            isMatch(msgLower, "404") -> when (domain) {
                "admin" -> "Admin service not available right now."
                "chat" -> "Chat service unavailable right now."
                "roleplay" -> "Roleplay service not available right now."
                "pronunciation" -> "Pronunciation service temporarily unavailable."
                else -> "Service not available right now."
            }
            isMatch(msgLower, "500", "internal server error") -> "Server error. Please try again."
            isMatch(msgLower, "enoent") -> "There was a problem with a required file. Please retry."
            else -> when (domain) {
                "pronunciation" -> "Something went wrong while analyzing your pronunciation. Please try again."
                "chat" -> "Something went wrong. Please try again."
                "roleplay" -> "Something went wrong. Please try again."
                "admin" -> "Failed to load dashboard. Please try again."
                "auth" -> if (raw.contains("Invalid credentials", true) || raw.contains("Email already exists", true)) raw else "Authentication failed. Please try again."
                else -> "Something went wrong. Please try again."
            }
        }
        val classification = classify(raw)
        telemetry(domain, raw, classification)
        return result
    }

    // Domain specific wrappers
    fun mapChatError(t: Throwable?): String = baseMap("chat", t)
    fun mapRoleplayError(t: Throwable?): String = baseMap("roleplay", t)
    fun mapAdminError(t: Throwable?): String = baseMap("admin", t)
    fun mapPronunciationError(t: Throwable?): String = baseMap("pronunciation", t)
    fun mapAuthError(t: Throwable?): String = baseMap("auth", t)
}
