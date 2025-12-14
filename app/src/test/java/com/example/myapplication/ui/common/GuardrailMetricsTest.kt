package com.example.myapplication.ui.common

import kotlin.test.Test
import kotlin.test.assertEquals

class GuardrailMetricsTest {
    @Test
    fun metrics_increment_and_snapshot() {
        GuardrailMetrics.reset()
        UiErrorMapper.mapChatError(Throwable("timeout while calling service"))
        UiErrorMapper.mapChatError(Throwable("timeout again"))
        UiErrorMapper.mapAdminError(Throwable("500 Internal Server Error"))
        val snap = GuardrailMetrics.snapshot()
        assertEquals(2, snap["chat:timeout"])
        assertEquals(1, snap["admin:server_error"])
    }
}

