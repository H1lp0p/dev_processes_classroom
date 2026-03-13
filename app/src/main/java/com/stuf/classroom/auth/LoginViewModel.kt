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

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val isLoggedIn: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val authManager: AuthManager,
    @MainDispatcher private val dispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

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

    fun onLoginClick() {
        val current = _uiState.value
        var emailError: String? = null
        var passwordError: String? = null

        if (current.email.isBlank()) {
            emailError = "Email is required"
        } else if (!current.email.contains("@")) {
            emailError = "Invalid email"
        }

        if (current.password.length < 6) {
            passwordError = "Password is too short"
        }

        if (emailError != null || passwordError != null) {
            _uiState.value = current.copy(
                emailError = emailError,
                passwordError = passwordError,
            )
            return
        }

        _uiState.value = current.copy(
            isLoading = true,
            emailError = null,
            passwordError = null,
            generalError = null,
        )

        viewModelScope.launch(dispatcher) {
            val result = authRepository.login(
                email = _uiState.value.email.trim(),
                password = _uiState.value.password,
            )

            when (result) {
                is DomainResult.Success -> {
                    handleSuccess(result.value)
                }

                is DomainResult.Failure -> {
                    handleFailure(result.error)
                }
            }
        }
    }

    private fun handleSuccess(session: AuthSession) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            emailError = null,
            passwordError = null,
            generalError = null,
            isLoggedIn = true,
        )
        authManager.onAuthSuccess(session)
    }

    private fun handleFailure(error: DomainError) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            generalError = error.toUserMessage(),
            isLoggedIn = false,
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

