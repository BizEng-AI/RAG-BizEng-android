package com.example.myapplication.uiPack.roleplay

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleplayScreen(vm: RoleplayVm) {
    val state by vm.state.collectAsState()

    // Log every state change for debugging
    LaunchedEffect(state) {
        android.util.Log.d("DEBUG_ROLEPLAY", "RoleplayScreen state changed: scenario=${state.scenario}, sessionStarted=${state.sessionStarted}")
    }

    // Stop TTS when leaving this screen
    DisposableEffect(Unit) {
        onDispose {
            vm.stopTts()
        }
    }

    // Handle back button press - return to scenario selection instead of exiting app
    androidx.activity.compose.BackHandler(enabled = state.sessionStarted) {
        android.util.Log.d("DEBUG_ROLEPLAY", "Back button pressed, returning to scenario selection")
        vm.stopTts()  // Stop TTS when going back
        vm.resetSession()
    }

    Column(Modifier.fillMaxSize()) {
        // Top bar with scenario selector and RAG toggle
        TopAppBar(
            title = { Text("Business Roleplay") },
            actions = {
                if (state.sessionStarted) {
                    IconButton(onClick = { vm.resetSession() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Reset")
                    }
                }
            }
        )

        if (!state.sessionStarted) {
            // Setup screen - choose scenario
            SetupScreen(vm = vm, state = state)
        } else {
            // Active roleplay session
            RoleplayConversation(vm = vm, state = state)
        }
    }
}

@Composable
private fun SetupScreen(vm: RoleplayVm, state: RoleplayUiState) {
    android.util.Log.d("DEBUG_ROLEPLAY", "SetupScreen recompose: scenario=${state.scenario}, sessionStarted=${state.sessionStarted}")
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(Modifier.height(16.dp))
            Text(
                "Choose a Business Scenario",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(24.dp))
        }

        // Scenario selection - clicking directly starts the roleplay
        items(vm.scenarios) { (key, label) ->
            val isSelected = state.scenario == key
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                onClick = {
                    android.util.Log.d("DEBUG_ROLEPLAY", "Card clicked: $key")
                    android.util.Log.d("DEBUG_ROLEPLAY", "Starting roleplay directly with scenario: $key")
                    vm.startSessionWithScenario(key)
                },
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
                ),
                border = if (isSelected)
                    androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                else
                    CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        when (key) {
                            "job_interview" -> Icons.Filled.Work
                            "client_meeting" -> Icons.Filled.People
                            "customer_complaint" -> Icons.Filled.Warning
                            "team_meeting" -> Icons.Filled.Groups
                            "business_phone_call" -> Icons.Filled.Phone
                            else -> Icons.Filled.Work
                        },
                        contentDescription = label,
                        modifier = Modifier.size(32.dp),
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.weight(1f))
                    if (isSelected) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))

            // Instructions
            Text(
                "Tap any scenario to begin",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RoleplayConversation(vm: RoleplayVm, state: RoleplayUiState) {
    Column(Modifier.fillMaxSize()) {
        // Scenario info banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    vm.scenarios.find { it.first == state.scenario }?.second ?: state.scenario,
                    style = MaterialTheme.typography.labelLarge
                )
                if (state.useRag) {
                    Spacer(Modifier.width(8.dp))
                    Text("• RAG Mode", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Messages
        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            reverseLayout = false
        ) {
            items(state.messages.filter { it.role != "system" }, key = { it.id }) { m ->
                val isUser = m.role == "user"
                val bubbleColor = if (isUser)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
                val align = if (isUser) Arrangement.End else Arrangement.Start

                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = align) {
                        Box(
                            Modifier
                                .padding(vertical = 6.dp)
                                .clip(MaterialTheme.shapes.large)
                                .background(bubbleColor)
                                .padding(12.dp)
                                .widthIn(max = 320.dp)
                        ) {
                            Text(if (m.streaming) "…" else m.text)
                        }
                    }

                    // Voice button for AI messages (not streaming, not empty)
                    if (!isUser && !m.streaming && m.text.isNotBlank()) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            IconButton(
                                onClick = { vm.speakMessage(m.text) },
                                modifier = Modifier.size(32.dp).padding(start = 4.dp)
                            ) {
                                Icon(
                                    Icons.Filled.VolumeUp,
                                    contentDescription = "Speak message",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Show correction/feedback if present
                    if (m.correction != null && !isUser) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            Card(
                                modifier = Modifier
                                    .padding(start = 8.dp, top = 4.dp, bottom = 8.dp)
                                    .widthIn(max = 320.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                )
                            ) {
                                Row(Modifier.padding(8.dp)) {
                                    Icon(
                                        Icons.Filled.Info,
                                        contentDescription = "Feedback",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        m.correction,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }

        // Error display
        if (state.error != null) {
            Text(
                state.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Input row with dynamic mic/send
        Surface(tonalElevation = 3.dp) {
            Column {
                if (state.recording) {
                    Text(
                        "Listening… tap mic to stop",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        textAlign = TextAlign.Center,
                        color = Color.Red,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = state.input,
                        onValueChange = vm::onInputChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Your response…") },
                        singleLine = false,
                        minLines = 1,
                        maxLines = 5,
                        enabled = !state.sending && !state.recording
                    )
                    Spacer(Modifier.width(8.dp))

                    val showSend = state.input.isNotBlank()
                    val iconTint = if (showSend)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSecondaryContainer
                    val bg = if (showSend)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.secondaryContainer

                    FilledIconButton(
                        onClick = { if (showSend) vm.send() else vm.onMicTapped() },
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = bg),
                        enabled = !state.sending
                    ) {
                        AnimatedContent(showSend, label = "send-mic") { send ->
                            if (send) Icon(Icons.Filled.Send, contentDescription = "Send", tint = iconTint)
                            else Icon(Icons.Filled.Mic, contentDescription = "Mic", tint = iconTint)
                        }
                    }
                }
            }
        }
    }
}

