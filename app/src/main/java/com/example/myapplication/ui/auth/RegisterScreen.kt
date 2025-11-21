package com.example.myapplication.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var groupNumber by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    // Reset state when screen is displayed
    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    val isLoading = uiState is AuthUiState.Loading

    val passwordsMatch = password == confirmPassword
    val canRegister = email.isNotBlank() &&
            password.isNotBlank() &&
            displayName.isNotBlank() &&
            passwordsMatch &&
            password.length >= 6

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        AuthTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = "Display Name",
            modifier = Modifier
        )

        Spacer(modifier = Modifier.height(16.dp))

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
            isPassword = true,
            supportingText = "Minimum 6 characters",
            modifier = Modifier
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirm Password",
            isPassword = true,
            modifier = Modifier
        )

        if (confirmPassword.isNotEmpty() && !passwordsMatch) {
            Text(
                text = "Passwords don't match",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = groupNumber,
            onValueChange = { groupNumber = it },
            label = "Group Number (Optional)",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (!isLoading) {
                    viewModel.register(
                        email.trim(),
                        password,
                        displayName.trim(),
                        groupNumber.ifBlank { null },
                        onRegisterSuccess
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && canRegister
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Register")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { if (!isLoading) onNavigateToLogin() },
            enabled = !isLoading
        ) {
            Text("Already have an account? Login")
        }

        if (uiState is AuthUiState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = (uiState as AuthUiState.Error).message,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
