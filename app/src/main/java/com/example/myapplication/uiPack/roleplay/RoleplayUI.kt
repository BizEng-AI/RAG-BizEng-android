package com.example.myapplication.uiPack.roleplay

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.components.TypingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegacyRoleplayScreen(vm: RoleplayVm) {
    val state by vm.state.collectAsState()

    // Log every state change for debugging
    LaunchedEffect(state) {
        android.util.Log.d("DEBUG_ROLEPLAY", "RoleplayScreen state changed: scenario=${state.scenario}, sessionStarted=${state.sessionStarted}")
    }

    // Stop TTS when leaving this screen
    DisposableEffect(Unit) {
        onDispose { vm.stopTts() }
    }

    // Handle back button press - return to scenario selection instead of exiting app
    androidx.activity.compose.BackHandler(enabled = state.sessionStarted) {
        android.util.Log.d("DEBUG_ROLEPLAY", "Back button pressed, returning to scenario selection")
        vm.stopTts()
        vm.resetSession()
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Roleplay", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            // Removed RAG toggle (not needed)
        }

        // Messages or scenario selector
        if (state.sessionStarted) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                items(state.messages, key = { it.id }) { m ->
                    val isUser = m.role == "user"
                    val bubbleColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    val align = if (isUser) Arrangement.End else Arrangement.Start

                    Column {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = align) {
                            Box(
                                Modifier
                                    .padding(vertical = 6.dp)
                                    .clip(MaterialTheme.shapes.large)
                                    .background(bubbleColor)
                                    .padding(12.dp)
                                    .widthIn(max = 340.dp)
                            ) {
                                if (m.streaming) TypingIndicator(Modifier.padding(vertical = 4.dp)) else Text(m.text)
                            }
                        }

                        // Show feedback under USER messages in pinkish color
                        if (isUser && m.correction != null) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFFFE4E1) // Pinkish color (misty rose)
                                    ),
                                    modifier = Modifier
                                        .padding(start = 40.dp, top = 4.dp)
                                        .widthIn(max = 340.dp)
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(
                                            "Feedback",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFD81B60) // Dark pink for title
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            m.correction,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF880E4F) // Deep pink for text
                                        )
                                    }
                                }
                            }
                        }

                        // Show speaker button under AI messages
                        if (!isUser && !m.streaming && m.text.isNotBlank()) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                                IconButton(onClick = { vm.speakMessage(m.text) }, modifier = Modifier.size(32.dp).padding(start = 4.dp)) {
                                    Icon(Icons.Filled.VolumeUp, contentDescription = "Speak", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        } else {
            // Scrollable scenario list (no clipping of last card)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Choose a scenario", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 8.dp))
                }
                items(vm.scenarios.toList()) { (id, title) ->
                    Card(onClick = { vm.startSessionWithScenario(id) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            val desc = when (id) {
                                "job_interview" -> "Practice common interview questions and answers."
                                "client_meeting" -> "Lead a client meeting with confidence and clarity."
                                "customer_complaint" -> "Handle complaints professionally and empathetically."
                                "team_meeting" -> "Run or participate in team meetings effectively."
                                "business_call" -> "Conduct professional business phone calls."
                                else -> null
                            }
                            desc?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                    }
                }
                item { Spacer(Modifier.height(12.dp)) }
            }
        }

        // Conventional input bar: mic when empty, send when has text; keep above keyboard without extra gap
        Surface(tonalElevation = 2.dp, color = MaterialTheme.colorScheme.surface) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tfColors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onBackground,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                )

                TextField(
                    value = state.input,
                    onValueChange = vm::onInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type your message…") },
                    singleLine = false,
                    minLines = 1,
                    maxLines = 5,
                    colors = tfColors
                )
                Spacer(Modifier.width(8.dp))

                val showSend = state.input.isNotBlank()
                val iconTint = if (showSend) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                val bg = if (showSend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer

                FilledIconButton(
                    onClick = { if (showSend) vm.send() else vm.onMicTapped() },
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = bg)
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
