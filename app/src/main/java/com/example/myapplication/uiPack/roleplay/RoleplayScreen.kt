package com.example.myapplication.uiPack.roleplay

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.uiPack.roleplay.RoleplayVm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleplayScreen(vm: RoleplayVm = hiltViewModel()) {
    val state by vm.state.collectAsState()

    // Stop any ongoing TTS when leaving
    DisposableEffect(Unit) { onDispose { /* TTS stopped in VM */ } }

    BackHandler(enabled = state.sessionStarted) {
        vm.resetSession()
    }

    if (!state.sessionStarted) {
        ScenarioSelection(vm = vm, state = state, onSelect = { vm.startSessionWithScenario(it) })
    } else {
        RoleplayConversation(state = state, vm = vm, onBack = { vm.resetSession() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScenarioSelection(vm: RoleplayVm, state: RoleplayUiState, onSelect: (String) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Text(text = "Choose a Business Scenario", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(vm.scenarios) { (id, title) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(text = title, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
        state.error?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoleplayConversation(state: RoleplayUiState, vm: RoleplayVm, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Roleplay Session") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .imePadding()
                .navigationBarsPadding()
        ) {
            // Stage info
            state.stageDescription?.let {
                Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(text = it, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            // Messages
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
                reverseLayout = false
            ) {
                items(state.messages, key = { it.id }) { m ->
                    val isUser = m.role == "user"
                    val bubbleColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    val align = if (isUser) Arrangement.End else Arrangement.Start
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = align) {
                        Box(
                            Modifier
                                .padding(vertical = 6.dp)
                                .clip(MaterialTheme.shapes.large)
                                .background(bubbleColor)
                                .padding(12.dp)
                                .widthIn(max = 340.dp)
                        ) {
                            if (m.streaming) Text("…", style = MaterialTheme.typography.bodyMedium) else Text(m.text)
                        }
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
            // Input row (no RAG toggle per request)
            Surface(tonalElevation = 2.dp, color = MaterialTheme.colorScheme.surface) {
                Row(
                    Modifier.fillMaxWidth().padding(12.dp),
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
                        placeholder = { Text("Your reply…") },
                        singleLine = false,
                        minLines = 1,
                        maxLines = 5,
                        colors = tfColors
                    )
                    Spacer(Modifier.width(8.dp))
                    val showSend = state.input.isNotBlank()
                    FilledIconButton(
                        onClick = { if (showSend) vm.send() else vm.startSession() },
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = if (showSend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        AnimatedContent(showSend, label = "send-mic-roleplay") { send ->
                            if (send) Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send") else Icon(Icons.Filled.Mic, contentDescription = "Start")
                        }
                    }
                }
                if (state.error != null) {
                    Text(
                        state.error,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
