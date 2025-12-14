package com.example.myapplication.ui.common

import kotlin.test.Test
import kotlin.test.assertEquals
import java.net.UnknownHostException

class UiErrorMapperTest {

    @Test
    fun chat_unknown_host_maps_to_network_message() {
        val msg = UiErrorMapper.mapChatError(UnknownHostException("Failed to resolve host"))
        assertEquals("Cannot reach server. Check your internet connection.", msg)
    }

    @Test
    fun chat_content_filter_maps_to_blocked_message() {
        val msg = UiErrorMapper.mapChatError(Throwable("content management policy violation"))
        assertEquals("This message was blocked. Please rephrase and try again.", msg)
    }

    @Test
    fun roleplay_404_maps_to_roleplay_not_available() {
        val msg = UiErrorMapper.mapRoleplayError(Throwable("HTTP 404 /roleplay/turn"))
        assertEquals("Roleplay service not available right now.", msg)
    }

    @Test
    fun admin_500_maps_to_server_error() {
        val msg = UiErrorMapper.mapAdminError(Throwable("500 Internal Server Error"))
        assertEquals("Server error. Please try again.", msg)
    }

    @Test
    fun pronunciation_small_error_maps_to_generic_pronunciation_message() {
        val msg = UiErrorMapper.mapPronunciationError(Throwable("random parse failure"))
        assertEquals("Something went wrong while analyzing your pronunciation. Please try again.", msg)
    }

    @Test
    fun auth_unauthorized_maps_to_session_expired() {
        val msg = UiErrorMapper.mapAuthError(Throwable("401 Unauthorized"))
        assertEquals("Session expired. Please log in again.", msg)
    }
}

