package com.example.myapplication.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.remote.auth.*
import com.example.myapplication.ui.common.UiErrorMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    // Observable login state
    private val _isLoggedIn = MutableStateFlow(authRepository.isLoggedIn())
    val isLoggedInState: StateFlow<Boolean> = _isLoggedIn

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (_uiState.value is AuthUiState.Loading) return@launch
            _uiState.value = AuthUiState.Loading

            authRepository.login(email, password)
                .onSuccess {
                    _uiState.value = AuthUiState.Success
                    _isLoggedIn.value = true  // Update login state
                    onSuccess()
                }
                .onFailure { error ->
                    val userMessage = UiErrorMapper.mapAuthError(error)
                    _uiState.value = AuthUiState.Error(userMessage)
                }
        }
    }

    fun register(
        email: String,
        password: String,
        displayName: String,
        groupNumber: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (_uiState.value is AuthUiState.Loading) return@launch
            _uiState.value = AuthUiState.Loading

            authRepository.register(email, password, displayName, groupNumber)
                .onSuccess {
                    _uiState.value = AuthUiState.Success
                    _isLoggedIn.value = true  // Update login state
                    onSuccess()
                }
                .onFailure { error ->
                    val userMessage = UiErrorMapper.mapAuthError(error)
                    _uiState.value = AuthUiState.Error(userMessage)
                }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
                .onSuccess {
                    _isLoggedIn.value = false  // Update login state
                    onSuccess()
                }
        }
    }

    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()

    fun isAdmin(): Boolean = authRepository.isAdmin()

    fun getUserName(): String? = authRepository.getUserName()

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    // New: validate session on startup; clears tokens if invalid
    suspend fun validateSession(): Boolean {
        return if (authRepository.isLoggedIn()) {
            val ok = authRepository.getProfile().isSuccess
            if (!ok) {
                authRepository.logout()
                _isLoggedIn.value = false
            } else {
                _isLoggedIn.value = true
            }
            ok
        } else {
            _isLoggedIn.value = false
            false
        }
    }
}
