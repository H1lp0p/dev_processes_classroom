package com.stuf.classroom.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.repository.AuthRepository
import com.stuf.domain.repository.AuthSession
import com.stuf.classroom.di.MainDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class RegisterUiState(
    val credentials: String = "",
    val email: String = "",
    val password: String = "",
    val repeatPassword: String = "",
    val isLoading: Boolean = false,
    val credentialsError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val repeatPasswordError: String? = null,
    val generalError: String? = null,
    val isRegistered: Boolean = false,
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val authManager: AuthManager,
    @MainDispatcher private val dispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun onCredentialsChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            credentials = value,
            credentialsError = null,
            generalError = null,
        )
    }

    fun onEmailChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            email = value,
            emailError = null,
            generalError = null,
        )
    }

    fun onPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            password = value,
            passwordError = null,
            generalError = null,
        )
    }

    fun onRepeatPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            repeatPassword = value,
            repeatPasswordError = null,
            generalError = null,
        )
    }

    fun onRegisterClick() {
        val current = _uiState.value
        var credentialsError: String? = null
        var emailError: String? = null
        var passwordError: String? = null
        var repeatPasswordError: String? = null

        if (current.credentials.isBlank()) {
            credentialsError = "Required"
        }

        if (current.email.isBlank()) {
            emailError = "Email is required"
        } else if (!current.email.contains("@")) {
            emailError = "Invalid email"
        }

        if (current.password.length < 6) {
            passwordError = "Password is too short"
        }

        if (current.repeatPassword.isBlank()) {
            repeatPasswordError = "Required"
        } else if (current.password != current.repeatPassword) {
            repeatPasswordError = "Passwords do not match"
        }

        if (credentialsError != null ||
            emailError != null ||
            passwordError != null ||
            repeatPasswordError != null
        ) {
            _uiState.value = current.copy(
                credentialsError = credentialsError,
                emailError = emailError,
                passwordError = passwordError,
                repeatPasswordError = repeatPasswordError,
            )
            return
        }

        _uiState.value = current.copy(
            isLoading = true,
            credentialsError = null,
            emailError = null,
            passwordError = null,
            repeatPasswordError = null,
            generalError = null,
        )

        viewModelScope.launch(dispatcher) {
            val result = authRepository.register(
                credentials = _uiState.value.credentials.trim(),
                email = _uiState.value.email.trim(),
                password = _uiState.value.password,
            )

            when (result) {
                is DomainResult.Success -> handleSuccess(result.value)
                is DomainResult.Failure -> handleFailure(result.error)
            }
        }
    }

    private fun handleSuccess(session: AuthSession) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            credentialsError = null,
            emailError = null,
            passwordError = null,
            repeatPasswordError = null,
            generalError = null,
            isRegistered = true,
        )
        authManager.onAuthSuccess(session)
    }

    private fun handleFailure(error: DomainError) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            generalError = error.toUserMessage(),
            isRegistered = false,
        )
    }

    private fun DomainError.toUserMessage(): String =
        when (this) {
            is DomainError.Validation -> this.message ?: "Validation error"
            DomainError.Unauthorized -> "Unauthorized"
            is DomainError.Network -> cause?.message?.let { "Network error: $it" } ?: "Network error"
            is DomainError.Unknown -> cause?.message?.let { "Error: $it" } ?: "Unknown error"
            else -> "Unknown error"
        }
}

