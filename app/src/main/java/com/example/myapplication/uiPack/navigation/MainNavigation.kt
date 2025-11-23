package com.example.myapplication.uiPack.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.uiPack.chat.ChatScreen
import com.example.myapplication.uiPack.chat.ChatVm
import com.example.myapplication.uiPack.roleplay.RoleplayScreen
import com.example.myapplication.uiPack.roleplay.RoleplayVm
import com.example.myapplication.uiPack.pronunciation.PronunciationScreen
import com.example.myapplication.uiPack.pronunciation.PronunciationVm
import com.example.myapplication.ui.auth.AuthViewModel
import com.example.myapplication.ui.auth.LoginScreen
import com.example.myapplication.ui.auth.RegisterScreen
import com.example.myapplication.ui.admin.AdminDashboardScreen
import com.example.myapplication.ui.admin.AdminDashboardViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.myapplication.data.repository.TrackingRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import java.util.TimeZone
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.myapplication.data.remote.dto.AttemptDto
import com.example.myapplication.data.repository.AuthRepository
import kotlinx.coroutines.delay
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.runtime.rememberCoroutineScope
import android.util.Log

sealed class NavDestination(val route: String, val title: String, val icon: ImageVector) {
    object Splash : NavDestination("splash", "Splash", Icons.Filled.HourglassEmpty)
    object Login : NavDestination("login", "Login", Icons.Filled.AccountCircle)
    object Register : NavDestination("register", "Register", Icons.Filled.PersonAdd)
    object Home : NavDestination("home", "Home", Icons.Filled.Home)
    object Chat : NavDestination("chat", "Chat", Icons.AutoMirrored.Filled.Message)
    object Roleplay : NavDestination("roleplay", "Roleplay", Icons.Filled.School)
    object Pronunciation : NavDestination("pronunciation", "Pronunciation", Icons.Filled.RecordVoiceOver)
    object Admin : NavDestination("admin", "Admin", Icons.Filled.BarChart)
    object Profile : NavDestination("profile", "Profile", Icons.Filled.Person)
}

@Composable
fun AppNavigation(onExit: () -> Unit) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()

    // Only intercept back on non-Home routes; let child handle Home specifics
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    if (currentRoute != NavDestination.Home.route) {
        BackHandler {
            when (currentRoute) {
                NavDestination.Splash.route -> onExit()
                NavDestination.Login.route -> onExit()
                NavDestination.Register.route -> navController.popBackStack()
                else -> if (!navController.popBackStack()) onExit()
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = NavDestination.Splash.route
    ) {
        // Splash: validate session once and route accordingly
        composable(NavDestination.Splash.route) {
            LaunchedEffect(Unit) {
                android.util.Log.d("AppNavigation", "🟦 Splash: validating session…")
                val ok = authViewModel.validateSession()
                if (ok) {
                    android.util.Log.d("AppNavigation", "   → Valid session: go Home")
                    navController.navigate(NavDestination.Home.route) {
                        popUpTo(NavDestination.Splash.route) { inclusive = true }
                    }
                } else {
                    android.util.Log.d("AppNavigation", "   → No session: go Login")
                    navController.navigate(NavDestination.Login.route) {
                        popUpTo(NavDestination.Splash.route) { inclusive = true }
                    }
                }
            }
            // Minimal splash UI
            Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(Modifier.padding(24.dp))
            }
        }

        composable(NavDestination.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(NavDestination.Home.route) {
                        popUpTo(NavDestination.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(NavDestination.Register.route)
                }
            )
        }

        composable(NavDestination.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(NavDestination.Home.route) {
                        popUpTo(NavDestination.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavDestination.Home.route) {
            MainNavigation(
                onLogout = {
                    authViewModel.logout {
                        navController.navigate(NavDestination.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onExit = onExit,
                onOpenProfile = { navController.navigate(NavDestination.Profile.route) }
            )
        }

        composable(NavDestination.Profile.route) {
            StudentProfileScreen(onBack = { navController.popBackStack() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(
    onLogout: () -> Unit,
    onExit: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val isLoggedIn by authViewModel.isLoggedInState.collectAsState()
    var isAdmin by remember { mutableStateOf(authViewModel.isAdmin()) }
    var selectedTab by remember { mutableStateOf<NavDestination>(NavDestination.Chat) }

    // Debounce logout to avoid multiple presses
    var menuExpanded by remember { mutableStateOf(false) }
    var loggingOut by remember { mutableStateOf(false) }

    // Provide VM refs for back handling
    var pronunciationVmRef: PronunciationVm? = null
    var pronunciationRecordMode by remember { mutableStateOf(false) }
    var isInAdminDetailView by remember { mutableStateOf(false) }

    LaunchedEffect(isLoggedIn) {
        val newValue = authViewModel.isAdmin()
        if (newValue != isAdmin) {
            isAdmin = newValue
            if (!isAdmin && selectedTab == NavDestination.Admin) {
                selectedTab = NavDestination.Chat
            }
        }
        // If user is logged out elsewhere, don't leave a blank screen
        if (!isLoggedIn) {
            // No-op here; AppNavigation handles redirect on explicit logout
        }
    }

    val destinations = remember(isAdmin) {
        buildList {
            add(NavDestination.Chat)
            add(NavDestination.Roleplay)
            add(NavDestination.Pronunciation)
            if (isAdmin) add(NavDestination.Admin)
        }
    }

    // Back behavior
    BackHandler {
        Log.d("MainNavigation", "Back pressed; selectedTab=${'$'}{selectedTab.route}, isAdmin=${'$'}isAdmin, isInAdminDetailView=${'$'}isInAdminDetailView, pronunciationRecordMode=${'$'}pronunciationRecordMode")
        when {
            selectedTab == NavDestination.Admin && isInAdminDetailView -> {
                Log.d("MainNavigation", "Back delegated to Admin detail screen")
            }
            selectedTab == NavDestination.Pronunciation && pronunciationRecordMode -> {
                Log.d("MainNavigation", "Back resetting Pronunciation from record mode")
                pronunciationVmRef?.resetToExampleMode()
            }
            else -> {
                Log.d("MainNavigation", "Back calling onExit() from tab=${'$'}{selectedTab.route}")
                onExit()
            }
        }
    }

    // Run logout once when requested
    LaunchedEffect(loggingOut) {
        if (loggingOut) {
            onLogout()
            loggingOut = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Explicitly non-interactive title to avoid accidental taps causing navigation
                    Text("BizEng", modifier = Modifier.semantics { disabled() }, color = MaterialTheme.colorScheme.onSurface)
                },
                actions = {
                    authViewModel.getUserName()?.let { name ->
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Profile") },
                            onClick = { menuExpanded = false; onOpenProfile() },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(if (loggingOut) "Logging out…" else "Logout") },
                            onClick = {
                                if (!loggingOut) {
                                    menuExpanded = false
                                    loggingOut = true
                                }
                            },
                            enabled = !loggingOut,
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) }
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = selectedTab == destination,
                        onClick = {
                            Log.d("MainNavigation", "Bottom nav click: ${'$'}{destination.route}, was=${'$'}{selectedTab.route}, isAdmin=${'$'}isAdmin")
                            selectedTab = destination
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.title,
                                modifier = Modifier.size(26.dp)
                            )
                        },
                        label = {
                            Text(
                                text = destination.title,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
        ) {
            when (selectedTab) {
                NavDestination.Chat -> {
                    Log.d("MainNavigation", "Rendering Chat tab")
                    val vm: ChatVm = hiltViewModel()
                    ChatScreen(vm = vm)
                }
                NavDestination.Roleplay -> {
                    Log.d("MainNavigation", "Rendering Roleplay tab")
                    val vm: RoleplayVm = hiltViewModel()
                    RoleplayScreen(vm = vm)
                }
                NavDestination.Pronunciation -> {
                    Log.d("MainNavigation", "Rendering Pronunciation tab")
                    val vm: PronunciationVm = hiltViewModel()
                    pronunciationVmRef = vm
                    val pState by vm.state.collectAsState()
                    pronunciationRecordMode = !pState.showExampleMode
                    PronunciationScreen(vm = vm)
                }
                NavDestination.Admin -> {
                    Log.d("MainNavigation", "Rendering Admin tab; isAdmin=${'$'}isAdmin")
                    if (isAdmin) {
                        val adminVm: AdminDashboardViewModel = hiltViewModel()
                        Log.d("MainNavigation", "AdminDashboardViewModel resolved: ${'$'}adminVm")
                        AdminDashboardScreen(
                            viewModel = adminVm,
                            onNavigationStateChange = { inDetail ->
                                Log.d("MainNavigation", "Admin nav state change: inDetail=${'$'}inDetail")
                                isInAdminDetailView = inDetail
                            }
                        )
                    } else {
                        Log.w("MainNavigation", "Non-admin user selected Admin tab; falling back to Chat")
                        selectedTab = NavDestination.Chat
                        val vm: ChatVm = hiltViewModel()
                        ChatScreen(vm = vm)
                    }
                }
                else -> {
                    Log.w("MainNavigation", "Unexpected tab=${'$'}{selectedTab.route}; falling back to Chat")
                    selectedTab = NavDestination.Chat
                    val vm: ChatVm = hiltViewModel()
                    ChatScreen(vm = vm)
                }
            }
        }
    }
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val trackingRepository: TrackingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    data class ProfileHeader(val name: String?, val group: String?)
    data class ProfileData(
        val header: ProfileHeader,
        val progress: com.example.myapplication.data.remote.dto.ProgressSummaryDto,
        val days: Int,
        val attempts: List<AttemptDto>
    )
    sealed class UiState {
        object Loading : UiState()
        data class Ready(val data: ProfileData) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _ui = MutableStateFlow<UiState>(UiState.Loading)
    val ui: StateFlow<UiState> = _ui

    private var lastLoadedAt: Long = 0L
    private val ttlMs = 60_000L

    fun load(days: Int = 30, force: Boolean = false) {
        if (!force && System.currentTimeMillis() - lastLoadedAt < ttlMs) return
        _ui.value = UiState.Loading
        viewModelScope.launch {
            val headerRes = authRepository.getProfile()
            val progressRes = trackingRepository.getMyProgress(days = days)
            val attemptsRes = trackingRepository.getMyAttempts(limit = 50, offset = 0, days = days)
            if (headerRes.isSuccess && progressRes.isSuccess && attemptsRes.isSuccess) {
                val p = headerRes.getOrThrow()
                val header = ProfileHeader(name = p.displayName, group = p.groupNumber)
                _ui.value = UiState.Ready(
                    ProfileData(header, progressRes.getOrThrow(), days, attemptsRes.getOrThrow())
                )
                lastLoadedAt = System.currentTimeMillis()
            } else {
                val reason = headerRes.exceptionOrNull()?.message ?: progressRes.exceptionOrNull()?.message ?: attemptsRes.exceptionOrNull()?.message ?: "Failed to load"
                _ui.value = UiState.Error(reason)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(onBack: () -> Unit, vm: ProfileViewModel = hiltViewModel()) {
    val state by vm.ui.collectAsState()

    LaunchedEffect(Unit) { vm.load(30, force = true) }

    fun formatDuration(totalSec: Int): String {
        val hours = totalSec / 3600
        val minutes = (totalSec % 3600) / 60
        val seconds = totalSec % 60
        return if (hours > 0) String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
        else String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }

    fun parseIsoToMillis(iso: String): Long {
        return try {
            // Try with timezone Z or offset
            val cleaned = iso.substringBeforeLast(".").replace("Z", "")
            val patterns = listOf(
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss"
            )
            for (p in patterns) {
                try {
                    val sdf = SimpleDateFormat(p, Locale.getDefault())
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                    return sdf.parse(iso)?.time ?: System.currentTimeMillis()
                } catch (_: Exception) { }
            }
            // Fallback simple
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            sdf.parse(cleaned)?.time ?: System.currentTimeMillis()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }

    fun formatMillis(millis: Long): String {
        val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Activity") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        when (val ui = state) {
            is ProfileViewModel.UiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
            is ProfileViewModel.UiState.Error -> {
                Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Text(ui.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is ProfileViewModel.UiState.Ready -> {
                val data = ui.data
                var selectedDays by remember { mutableStateOf(data.days) }
                var refreshing by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()
                val refreshState = rememberSwipeRefreshState(isRefreshing = refreshing)

                Box(Modifier.fillMaxSize().padding(padding)) {
                    SwipeRefresh(
                        state = refreshState,
                        onRefresh = {
                            refreshing = true
                            scope.launch {
                                vm.load(selectedDays, force = true)
                                kotlinx.coroutines.delay(350)
                                refreshing = false
                            }
                        }
                    ) {
                        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Header from /me
                            item {
                                Card { Column(Modifier.padding(12.dp)) {
                                    Text(data.header.name ?: "", style = MaterialTheme.typography.titleLarge)
                                    data.header.group?.let { Text("Group: $it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                } }
                            }
                            // Range selector
                            item {
                                Text("Activity Range", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(7, 30, 90).forEach { d ->
                                        val selected = selectedDays == d
                                        FilterChip(selected = selected, onClick = { selectedDays = d; vm.load(d, force = true) }, label = { Text("${d}d") })
                                    }
                                }
                            }

                            // Overview
                            item {
                                Text("Overview", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(8.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column { Text("Attempts", style = MaterialTheme.typography.labelSmall); Text("${data.progress.totals.attempts}", style = MaterialTheme.typography.titleMedium) }
                                    Column { Text("Completed", style = MaterialTheme.typography.labelSmall); Text("${data.progress.totals.completed}", style = MaterialTheme.typography.titleMedium) }
                                    Column { Text("Avg score", style = MaterialTheme.typography.labelSmall); Text(String.format(Locale.getDefault(), "%.1f", data.progress.totals.avgScore), style = MaterialTheme.typography.titleMedium) }
                                    Column { Text("Minutes", style = MaterialTheme.typography.labelSmall); Text("${data.progress.totals.totalMinutes}", style = MaterialTheme.typography.titleMedium) }
                                }
                            }

                            // By type (if present)
                            if (data.progress.byType.isNotEmpty()) {
                                item {
                                    Text("By type", style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(8.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        val labels = listOf("chat" to "Chat", "roleplay" to "Roleplay", "pronunciation" to "Pronunciation")
                                        labels.forEach { (key, label) ->
                                            data.progress.byType[key]?.let { s ->
                                                Card { Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, style = MaterialTheme.typography.titleMedium); Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) { Text("Attempts: ${s.attempts}"); Text("Avg: ${String.format(Locale.getDefault(), "%.1f", s.avgScore)}") } } }
                                            }
                                        }
                                    }
                                }
                            }

                            // Attempts list from /tracking/my-attempts (correct path)
                            if (data.attempts.isNotEmpty()) {
                                item { Text("Recent attempts", style = MaterialTheme.typography.titleMedium) }
                                items(data.attempts) { a ->
                                    Card { Column(Modifier.padding(12.dp)) {
                                        Text(a.exerciseType.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }, style = MaterialTheme.typography.titleMedium)
                                        Spacer(Modifier.height(4.dp))
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            a.score?.let { Text("Score: ${String.format(Locale.getDefault(), "%.1f", it)}") } // score 0-100 as-is
                                            a.durationSeconds?.let { Text("Duration: ${formatDuration(it)}") }
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(formatMillis(parseIsoToMillis(a.startedAt)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    } }
                                }
                            } else {
                                item { Text("No recent attempts yet", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            }
                        }
                    }
                }
            }
        }
    }
}
