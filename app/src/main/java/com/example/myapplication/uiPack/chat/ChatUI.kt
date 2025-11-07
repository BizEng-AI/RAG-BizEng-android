package com.example.myapplication.uiPack.chat

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ChatScreen(vm: ChatVm) {
    val state by vm.state.collectAsState()

    // Stop TTS when leaving this screen
    DisposableEffect(Unit) {
        onDispose {
            vm.stopTts()
        }
    }

    Column(Modifier.fillMaxSize()) {
        // Top bar: “Ground in book” switch (RAG)
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ground in book", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(8.dp))
            Switch(checked = state.grounded, onCheckedChange = { vm.toggleGrounding() })
            Spacer(Modifier.weight(1f))
            if (state.error != null) Text(state.error!!, color = MaterialTheme.colorScheme.error)
        }

        // Messages
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
            reverseLayout = false
        ) {
            items(state.messages, key = { it.id }) { m ->
                val bubbleColor = if (m.role == "user") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                val align = if (m.role == "user") Arrangement.End else Arrangement.Start
                Row(Modifier.fillMaxWidth(), horizontalArrangement = align) {
                    Column {
                        Box(
                            Modifier.padding(vertical = 6.dp)
                                .clip(MaterialTheme.shapes.large)
                                .background(bubbleColor)
                                .padding(12.dp)
                                .widthIn(max = 320.dp)
                        ) {
                            Text(if (m.streaming) "…" else m.text)
                        }
                        // Voice button only for assistant messages that are not streaming
                        if (m.role == "assistant" && !m.streaming && m.text.isNotBlank()) {
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
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }

        // Input row with dynamic mic/send
        Surface(tonalElevation = 3.dp) {
            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.input,
                    onValueChange = vm::onInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message…") },
                    singleLine = false,
                    minLines = 1,
                    maxLines = 5
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
            if (state.recording) {
                Text(
                    "Listening… tap mic to stop",
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    textAlign = TextAlign.Center,
                    color = Color.Red
                )
            }
        }
    }
}
