package com.bizenglish.app.uiPack.pronunciation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PronunciationScreen(vm: PronunciationVm) {
    val state by vm.state.collectAsState()

    // Stop TTS when leaving this screen
    DisposableEffect(Unit) {
        onDispose {
            vm.stopTts()
        }
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Pronunciation Practice") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        if (state.showExampleMode) {
            // Mode 1: Type and Hear Pronunciation
            ExampleModeScreen(vm, state)
        } else {
            // Mode 2: Record and Get Feedback
            RecordModeScreen(vm, state)
        }
    }
}

@Composable
private fun ExampleModeScreen(vm: PronunciationVm, state: PronunciationUiState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Type a word or phrase to practice:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            OutlinedTextField(
                value = state.inputText,
                onValueChange = vm::onInputChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., schedule a meeting") },
                singleLine = false,
                maxLines = 3
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Hear Pronunciation Button
                Button(
                    onClick = { vm.speakPhrase() },
                    modifier = Modifier.weight(1f),
                    enabled = state.inputText.isNotBlank()
                ) {
                    Icon(Icons.Filled.VolumeUp, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Hear It")
                }

                // Practice Button
                Button(
                    onClick = { vm.onPracticeButtonClicked() },
                    modifier = Modifier.weight(1f),
                    enabled = state.inputText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Filled.Mic, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Practice")
                }
            }
        }

        // Error message
        if (state.error != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        state.error!!,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        item {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                "Suggested Business Phrases:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(vm.suggestedPhrases) { phrase ->
            OutlinedCard(
                onClick = { vm.setTargetPhrase(phrase) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.RecordVoiceOver,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        phrase,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RecordModeScreen(vm: PronunciationVm, state: PronunciationUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back button
        OutlinedButton(
            onClick = { vm.resetToExampleMode() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Back to Examples")
        }

        // Target phrase card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Practice saying:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    state.targetPhrase,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        // Hear example button
        Button(
            onClick = { vm.speakPhrase() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.VolumeUp, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Hear Example Pronunciation")
        }

        // Recording indicator
        if (state.recording) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.Red.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.FiberManualRecord,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Recording... Tap mic to stop",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Red
                    )
                }
            }
        }

        // Record button
        FilledTonalButton(
            onClick = { vm.onMicTapped() },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            enabled = !state.assessing,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = if (state.recording) Color.Red else MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                Icons.Filled.Mic,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                if (state.recording) "Tap to Stop" else "Tap to Record",
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Assessing indicator
        if (state.assessing) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Analyzing your pronunciation...")
                }
            }
        }

        // Error message
        if (state.error != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    state.error!!,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // Results
        state.result?.let { result ->
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Results:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Overall score
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                result.pronunciationScore >= 90 -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                result.pronunciationScore >= 70 -> Color(0xFFFFC107).copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.errorContainer
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "${result.pronunciationScore.toInt()}/100",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    result.pronunciationScore >= 90 -> Color(0xFF4CAF50)
                                    result.pronunciationScore >= 70 -> Color(0xFFFFC107)
                                    else -> MaterialTheme.colorScheme.error
                                }
                            )
                            Text("Overall Score", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // What you said
                item {
                    Card {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                "You said:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                result.transcript,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                // Detailed scores
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            ScoreRow("Accuracy", result.accuracyScore)
                            ScoreRow("Fluency", result.fluencyScore)
                            ScoreRow("Completeness", result.completenessScore)
                        }
                    }
                }

                // Feedback (red message like in roleplay)
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Row(Modifier.padding(12.dp)) {
                            Icon(
                                Icons.Filled.Lightbulb,
                                contentDescription = "Feedback",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                result.feedback,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

                // Detailed Feedback Section (NEW)
                result.detailedFeedback?.let { tips ->
                    if (tips.isNotEmpty()) {
                        item {
                            DetailedFeedbackSection(detailedFeedback = tips)
                        }
                    }
                }

                // Word-by-word breakdown with IPA and phonemes
                if (result.words.isNotEmpty()) {
                    item {
                        Text(
                            "📝 Word Analysis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(result.words) { word ->
                        // Show detailed breakdown for words that need practice
                        if (word.accuracyScore < 80 || word.errorType != null) {
                            PhonemeBreakdownCard(word)
                        } else {
                            // Simple card for good words
                            SimpleWordCard(word)
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ScoreRow(label: String, score: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            "${score.toInt()}/100",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

// ============================================================================
// NEW: ENHANCED PRONUNCIATION COMPONENTS WITH IPA TRANSCRIPTION
// ============================================================================

@Composable
private fun DetailedFeedbackSection(detailedFeedback: List<String>) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header (clickable to expand/collapse)
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "💡",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Detailed Tips (${detailedFeedback.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand"
                    )
                }
            }

            // Content (expandable)
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                    detailedFeedback.forEachIndexed { index, tip ->
                        if (index > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(end = 8.dp, top = 2.dp)
                            )
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PhonemeBreakdownCard(word: com.bizenglish.app.data.remote.dto.PronunciationWordDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                word.accuracyScore >= 80 -> MaterialTheme.colorScheme.surfaceVariant
                word.accuracyScore >= 60 -> Color(0xFFFF9800).copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Word header with score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = word.word,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // IPA transcription (NEW)
                    word.ipaExpected?.let { ipa ->
                        Text(
                            text = "/$ipa/",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Score badge
                val scoreColor = when {
                    word.accuracyScore >= 80 -> Color(0xFF4CAF50)
                    word.accuracyScore >= 60 -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                }

                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = scoreColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${word.accuracyScore.toInt()}/100",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = scoreColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Error type badge
            word.errorType?.let { errorType ->
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "⚠️ $errorType",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Phoneme breakdown
            word.phonemes?.let { phonemes ->
                if (phonemes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Phonemes:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Phoneme chips in a flow layout
                    PhonemeChipsRow(phonemes)
                }
            }

            // Word-specific feedback
            word.feedback?.let { feedback ->
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = feedback,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PhonemeChip(phoneme: com.bizenglish.app.data.remote.dto.PronunciationPhonemeDto) {
    val backgroundColor = when {
        phoneme.score >= 80 -> Color(0xFF4CAF50).copy(alpha = 0.2f)  // Green
        phoneme.score >= 60 -> Color(0xFFFF9800).copy(alpha = 0.2f)  // Orange
        else -> Color(0xFFF44336).copy(alpha = 0.2f)  // Red
    }

    val textColor = when {
        phoneme.score >= 80 -> Color(0xFF2E7D32)
        phoneme.score >= 60 -> Color(0xFFE65100)
        else -> Color(0xFFC62828)
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "/${phoneme.phoneme}/",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${phoneme.score.toInt()}",
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SimpleWordCard(word: com.bizenglish.app.data.remote.dto.PronunciationWordDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = word.word,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    word.ipaExpected?.let { ipa ->
                        Text(
                            text = "/$ipa/",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                text = "${word.accuracyScore.toInt()}/100",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PhonemeChipsRow(phonemes: List<com.bizenglish.app.data.remote.dto.PronunciationPhonemeDto>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        phonemes.forEach { phoneme ->
            PhonemeChip(phoneme)
        }
    }
}

