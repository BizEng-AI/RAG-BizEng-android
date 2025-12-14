package com.example.myapplication.uiPack.chat

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.components.TypingIndicator
import com.example.myapplication.ui.common.MicState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(vm: ChatVm) {
    val state by vm.state.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            vm.stopTts()
            vm.completeAttemptIfNeeded()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .navigationBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ground in book", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(8.dp))
            Switch(checked = state.grounded, onCheckedChange = { vm.toggleGrounding() })
            Spacer(Modifier.weight(1f))
        }
        HorizontalDivider()

        if (state.messages.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Start the conversation by typing below",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(state.messages, key = { it.id }) { m ->
                    val isUser = m.role == "user"
                    val bubbleColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    val align = if (isUser) Arrangement.End else Arrangement.Start
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = align) {
                        Column {
                            Box(
                                Modifier
                                    .padding(vertical = 4.dp)
                                    .clip(MaterialTheme.shapes.large)
                                    .background(bubbleColor)
                                    .padding(10.dp)
                                    .widthIn(max = 340.dp)
                            ) {
                                if (m.streaming) TypingIndicator(Modifier.padding(vertical = 2.dp)) else Text(m.text)
                            }
                            if (!isUser && !m.streaming && m.text.isNotBlank()) {
                                IconButton(
                                    onClick = { vm.speakMessage(m.text) },
                                    modifier = Modifier.size(28.dp).padding(start = 2.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Speak message",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = state.input,
                        onValueChange = vm::onInputChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Message…") },
                        singleLine = false,
                        minLines = 1,
                        maxLines = 5,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    val showSend = state.input.isNotBlank()
                    FilledIconButton(
                        onClick = { if (showSend) vm.send() else vm.onMicTapped() },
                        modifier = Modifier.size(44.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = when {
                                showSend -> MaterialTheme.colorScheme.primary
                                state.micState == MicState.Listening -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.secondaryContainer
                            }
                        )
                    ) {
                        AnimatedContent(showSend to state.micState, label = "send-mic") { (send, mic) ->
                            when {
                                send -> Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                                mic == MicState.Listening -> Icon(Icons.Filled.Mic, contentDescription = "Stop Listening", tint = MaterialTheme.colorScheme.error)
                                mic == MicState.Processing -> CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                mic == MicState.Error -> Icon(Icons.Filled.Mic, contentDescription = "Mic Error", tint = MaterialTheme.colorScheme.error)
                                else -> Icon(
                                    Icons.Filled.Mic,
                                    contentDescription = "Mic",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
                when (state.micState) {
                    MicState.Listening -> Text(
                        "Listening… tap mic to stop",
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                    MicState.Processing -> Text(
                        "Processing speech…",
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall
                    )
                    MicState.Error -> state.error?.let { err ->
                        Text(
                            err,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    else -> Unit
                }
                if (state.error != null && state.micState != MicState.Error) {
                    Text(
                        state.error!!,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
