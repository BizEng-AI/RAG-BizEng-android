package com.example.myapplication.ui.admin

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.components.BizEngCard
import com.example.myapplication.ui.components.SectionHeader
import com.example.myapplication.ui.components.StatCard
import com.example.myapplication.ui.theme.BizEngDesign
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel,
    onNavigationStateChange: (Boolean) -> Unit = {}  // Notify when in detail view
) {
    val state by viewModel.uiState.collectAsState()
    val section by viewModel.selectedSection.collectAsState()
    var selectedUserId by remember { mutableStateOf<Long?>(null) }

    // Notify parent about navigation state
    LaunchedEffect(selectedUserId) {
        onNavigationStateChange(selectedUserId != null)
    }

    // Handle back button when in detail view
    BackHandler(enabled = selectedUserId != null) {
        selectedUserId = null
    }

    when (val ui = state) {
        AdminDashboardUiState.Loading -> LoadingState()
        is AdminDashboardUiState.Error -> ErrorState(ui.message, onRetry = viewModel::retry)
        is AdminDashboardUiState.Success -> {
            if (selectedUserId != null) {
                StudentDetailScreen(
                    userId = selectedUserId!!,
                    viewModel = viewModel,
                    onBack = { selectedUserId = null }
                )
            } else {
                DashboardContent(
                    data = ui.data,
                    section = section,
                    onChangeSection = viewModel::setSection,
                    onRefresh = { viewModel.loadDashboard(force = true) },
                    onStudentClick = { userId -> selectedUserId = userId }
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(BizEngDesign.Spacing.elementVertical))
            Text(
                text = "Loading dashboard...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(BizEngDesign.Spacing.screenPadding),
        contentAlignment = Alignment.Center
    ) {
        BizEngCard {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(BizEngDesign.Spacing.elementVertical)
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(BizEngDesign.Spacing.elementVertical))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(BizEngDesign.Spacing.sectionVertical))
                Button(onClick = onRetry) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
private fun SectionChips(
    selected: AdminSection,
    onSelected: (AdminSection) -> Unit
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selected == AdminSection.Overview,
                onClick = { onSelected(AdminSection.Overview) },
                label = { Text("Overview") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selected == AdminSection.Students,
                onClick = { onSelected(AdminSection.Students) },
                label = { Text("Students") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selected == AdminSection.Groups,
                onClick = { onSelected(AdminSection.Groups) },
                label = { Text("Groups") },
                modifier = Modifier.weight(1f)
            )
        }
        Row(Modifier.fillMaxWidth()) {
            FilterChip(
                selected = selected == AdminSection.RecentAttempts,
                onClick = { onSelected(AdminSection.RecentAttempts) },
                label = { Text("Recent Activity") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DashboardContent(
    data: AdminDashboardData,
    section: AdminSection,
    onChangeSection: (AdminSection) -> Unit,
    onRefresh: () -> Unit,
    onStudentClick: (Long) -> Unit
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Rotation animation for refresh icon
    val infiniteTransition = rememberInfiniteTransition(label = "refresh")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isRefreshing) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(BizEngDesign.Spacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(BizEngDesign.Spacing.sectionVertical)
    ) {
        // Header with title and refresh
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Admin Dashboard",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Last updated ${formatTimestamp(data.lastUpdatedAtMillis)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = {
                        isRefreshing = true
                        onRefresh()
                        // Reset after animation
                        coroutineScope.launch {
                            delay(1000)
                            isRefreshing = false
                        }
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh data",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }
        }

        // Section tabs
        item {
            SectionChips(selected = section, onSelected = onChangeSection)
        }

        // Section content
        when (section) {
            AdminSection.Overview -> {
                item {
                    SectionHeader(
                        title = "Overview",
                        icon = Icons.Default.Dashboard
                    )
                }
                item { OverviewCards(data) }
            }
            AdminSection.Students -> {
                item {
                    SectionHeader(
                        title = "Students",
                        icon = Icons.Default.People
                    )
                }
                if (data.usersActivity.isNotEmpty()) {
                    items(data.usersActivity) { user ->
                        UserActivityCard(
                            user = user,
                            onClick = { onStudentClick(user.userId) }
                        )
                    }
                } else {
                    item {
                        Text(
                            "No student activity data",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            AdminSection.Groups -> {
                item {
                    SectionHeader(
                        title = "Groups",
                        icon = Icons.Default.Group
                    )
                }
                if (data.groupsActivity.isNotEmpty()) {
                    items(data.groupsActivity) { group ->
                        GroupActivityCard(group)
                    }
                } else {
                    item {
                        Text(
                            "No group activity data",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            AdminSection.RecentAttempts -> {
                item {
                    SectionHeader(
                        title = "Recent Activity",
                        icon = Icons.Default.History
                    )
                }
                if (data.recentAttempts.isNotEmpty()) {
                    items(data.recentAttempts) { attempt ->
                        RecentAttemptCard(attempt)
                    }
                } else {
                    item {
                        Text(
                            "No recent attempts",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewCards(data: AdminDashboardData) {
    // Calculate totals from actual data
    val totalUsers = data.usersActivity.size
    val totalAttempts = data.usersActivity.sumOf { it.totalExercises }
    val active = data.activeToday?.activeStudents ?: 0
    val totalDuration = data.usersActivity.sumOf { it.totalDurationSeconds }
    val hours = totalDuration / 3600
    val minutes = (totalDuration % 3600) / 60

    // 2x2 Grid layout for metrics
    Column(verticalArrangement = Arrangement.spacedBy(BizEngDesign.Spacing.elementVertical)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(BizEngDesign.Spacing.elementVertical)
        ) {
            StatCard(
                title = "Total Users",
                value = NumberFormat.getIntegerInstance().format(totalUsers),
                icon = Icons.Default.People,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Total Exercises",
                value = NumberFormat.getIntegerInstance().format(totalAttempts),
                icon = Icons.Default.Assignment,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(BizEngDesign.Spacing.elementVertical)
        ) {
            StatCard(
                title = "Active Today",
                value = NumberFormat.getIntegerInstance().format(active),
                icon = Icons.Default.TrendingUp,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Total Time",
                value = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m",
                icon = Icons.Default.Schedule,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun UserActivityCard(
    user: com.example.myapplication.data.remote.dto.UserActivitySummaryDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(Modifier.padding(16.dp)) {
            // Title: Display Name
            Text(
                text = user.displayName ?: "Unnamed Student",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Subtitle: Email
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Group info
            Text(
                text = "Group: ${user.groupName ?: "Unassigned"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            // Exercise breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ExerciseCount("🗣️ Pronunciation", user.pronunciationCount)
                ExerciseCount("💬 Chat", user.chatCount)
                ExerciseCount("🎭 Roleplay", user.roleplayCount)
            }

            Spacer(Modifier.height(12.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Exercises", style = MaterialTheme.typography.labelSmall)
                    Text(
                        "${user.totalExercises}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text("Total Duration", style = MaterialTheme.typography.labelSmall)
                    Text(
                        formatDuration(user.totalDurationSeconds),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                user.avgPronunciationScore?.let { score ->
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Avg Score", style = MaterialTheme.typography.labelSmall)
                        Text(
                            formatScore(score),
                            style = MaterialTheme.typography.bodyMedium,
                            color = when {
                                score >= 80 -> MaterialTheme.colorScheme.primary
                                score >= 60 -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun GroupActivityCard(group: com.example.myapplication.data.remote.dto.GroupActivitySummaryDto) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.groupName ?: "Unassigned",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${group.studentCount} students",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${group.totalExercises}",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            // Exercise breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ExerciseCount("🗣️ Pronunciation", group.pronunciationCount)
                ExerciseCount("💬 Chat", group.chatCount)
                ExerciseCount("🎭 Roleplay", group.roleplayCount)
            }

            Spacer(Modifier.height(12.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Duration", style = MaterialTheme.typography.labelSmall)
                    Text(
                        formatDuration(group.totalDurationSeconds),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                group.avgPronunciationScore?.let { score ->
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Avg Pronunciation", style = MaterialTheme.typography.labelSmall)
                        Text(
                            formatScore(score),
                            style = MaterialTheme.typography.bodyMedium,
                            color = when {
                                score >= 80 -> MaterialTheme.colorScheme.primary
                                score >= 60 -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentAttemptCard(attempt: com.example.myapplication.data.remote.dto.RecentAttemptDto) {
    BizEngCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = attempt.studentName ?: attempt.studentEmail,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        if (attempt.studentName != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = attempt.studentEmail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(BizEngDesign.Spacing.small))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(BizEngDesign.Spacing.small))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = attempt.exerciseType.capitalize(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            attempt.score?.let { score ->
                Text(
                    text = formatScore(score),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        score >= 80 -> com.example.rag.ui.theme.BizEngSuccess
                        score >= 60 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            }
        }

        attempt.startedAt?.let { timestamp ->
            Spacer(Modifier.height(4.dp))
            Text(
                text = formatTimestamp(parseTimestamp(timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ExerciseCount(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatDuration(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> if (secs == 0) "${minutes}m" else "${minutes}m ${secs}s"
        else -> "${secs}s"
    }
}

private fun formatTimestamp(millis: Long): String {
    val formatter = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
    return formatter.format(Date(millis))
}

private fun parseTimestamp(iso8601: String): Long {
    return try {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        formatter.timeZone = java.util.TimeZone.getTimeZone("UTC")
        formatter.parse(iso8601.substringBefore("Z").substringBefore("+"))?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}

private fun formatScore(score: Float): String = String.format(Locale.getDefault(), "%.1f", score)

private fun String.capitalize(): String = this.replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentDetailScreen(
    userId: Long,
    viewModel: AdminDashboardViewModel,
    onBack: () -> Unit
) {
    var userActivity by remember { mutableStateOf<com.example.myapplication.data.remote.dto.UserActivityResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        isLoading = true
        errorMessage = null
        viewModel.getUserActivity(userId, days = 30)
            .onSuccess { userActivity = it; isLoading = false }
            .onFailure { errorMessage = it.message; isLoading = false }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Activity") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                isLoading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                errorMessage != null -> {
                    Column(
                        Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(12.dp))
                        Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onBack) {
                            Text("Go Back")
                        }
                    }
                }
                userActivity != null -> {
                    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
                        // Student header
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        text = userActivity!!.user.displayName ?: "Unnamed Student",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = userActivity!!.user.email,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    userActivity!!.user.groupName?.let {
                                        Text(
                                            text = "Group: $it",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Exercise History (Last 30 Days)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        // Exercise items
                        val items = userActivity!!.items.sortedByDescending { it.startedAt }
                        if (items.isEmpty()) {
                            item {
                                Text(
                                    "No exercises completed yet",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 24.dp)
                                )
                            }
                        } else {
                            items(items) { item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = item.exerciseType.capitalize(),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            item.score?.let { score ->
                                                Text(
                                                    text = formatScore(score),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = when {
                                                        score >= 80 -> MaterialTheme.colorScheme.primary
                                                        score >= 60 -> MaterialTheme.colorScheme.tertiary
                                                        else -> MaterialTheme.colorScheme.error
                                                    }
                                                )
                                            }
                                        }

                                        // Remove exerciseId subtitle - users don't need to see internal IDs
                                        // item.exerciseId?.let {
                                        //     Text(
                                        //         text = it,
                                        //         style = MaterialTheme.typography.bodySmall,
                                        //         color = MaterialTheme.colorScheme.onSurfaceVariant
                                        //     )
                                        // }

                                        Spacer(Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            item.durationSeconds?.let { duration ->
                                                Text(
                                                    text = "Duration: ${formatDuration(duration)}",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                            item.pronunciationScore?.let { pronScore ->
                                                Text(
                                                    text = "Pronunciation: ${formatScore(pronScore)}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.tertiary
                                                )
                                            }
                                        }

                                        item.startedAt?.let { timestamp ->
                                            Text(
                                                text = formatTimestamp(parseTimestamp(timestamp)),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
