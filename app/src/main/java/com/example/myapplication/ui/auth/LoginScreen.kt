package com.example.myapplication.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    // Reset state when screen is displayed
    LaunchedEffect(Unit) { viewModel.resetState() }

    val isLoading = uiState is AuthUiState.Loading

    // Theme tokens
    val background = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val onBg = MaterialTheme.colorScheme.onBackground
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "BizEng Login",
            style = MaterialTheme.typography.headlineMedium,
            color = onBg,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            placeholder = "name@example.com",
            modifier = Modifier
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            placeholder = "••••••••",
            isPassword = true,
            modifier = Modifier
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (!isLoading) {
                    viewModel.login(email.trim(), password, onLoginSuccess)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = onPrimary
                )
            } else {
                Text("Login", color = onPrimary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { if (!isLoading) onNavigateToRegister() },
            enabled = !isLoading
        ) {
            Text(
                "Don't have an account? Register",
                color = primary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        if (uiState is AuthUiState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        val errorMsg = (uiState as AuthUiState.Error).message
                        val displayMsg = when {
                            errorMsg.contains("401") || errorMsg.contains("Unauthorized") -> "Incorrect email or password"
                            errorMsg.contains("404") || errorMsg.contains("not found", ignoreCase = true) -> "Unable to connect to server. Please try again later."
                            errorMsg.contains("timeout", ignoreCase = true) -> "Connection timeout. Check your internet connection."
                            errorMsg.contains("network", ignoreCase = true) || errorMsg.contains("connection", ignoreCase = true) -> "Network error. Please check your connection."
                            else -> errorMsg
                        }
                        Text(
                            text = displayMsg,
                            color = onBg,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
