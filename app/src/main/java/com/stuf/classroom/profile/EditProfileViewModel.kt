package com.stuf.classroom.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stuf.classroom.di.MainDispatcher
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.repository.CurrentUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val credentials: String = "",
    val email: String = "",
    val isLoading: Boolean = false,
    val credentialsError: String? = null,
    val emailError: String? = null,
    val generalError: String? = null,
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val currentUserRepository: CurrentUserRepository,
    @MainDispatcher private val dispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditProfileUiState(isLoading = true))
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events.asSharedFlow()

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, generalError = null)
        viewModelScope.launch(dispatcher) {
            when (val result = currentUserRepository.getCurrentUser()) {
                is DomainResult.Success ->
                    _uiState.value =
                        EditProfileUiState(
                            credentials = result.value.credentials,
                            email = result.value.email,
                            isLoading = false,
                        )
                is DomainResult.Failure ->
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            generalError = result.error.toUserMessage(),
                        )
            }
        }
    }

    fun onCredentialsChanged(value: String) {
        _uiState.value = _uiState.value.copy(credentials = value, credentialsError = null, generalError = null)
    }

    fun onEmailChanged(value: String) {
        _uiState.value = _uiState.value.copy(email = value, emailError = null, generalError = null)
    }

    fun onSave(onSuccess: () -> Unit) {
        val current = _uiState.value
        var credentialsError: String? = null
        var emailError: String? = null
        if (current.credentials.isBlank()) credentialsError = "Введите имя"
        if (current.email.isBlank() || !current.email.contains("@")) emailError = "Введите корректный email"
        if (credentialsError != null || emailError != null) {
            _uiState.value = current.copy(credentialsError = credentialsError, emailError = emailError)
            return
        }

        _uiState.value = current.copy(isLoading = true, generalError = null)
        viewModelScope.launch(dispatcher) {
            when (val result = currentUserRepository.updateCurrentUser(current.credentials.trim(), current.email.trim())) {
                is DomainResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit("Профиль обновлён")
                    onSuccess()
                }
                is DomainResult.Failure -> {
                    val message = result.error.toUserMessage()
                    _uiState.value = _uiState.value.copy(isLoading = false, generalError = message)
                    _events.emit(message)
                }
            }
        }
    }
}

private fun DomainError.toUserMessage(): String =
    when (this) {
        is DomainError.Validation -> message
        DomainError.Unauthorized -> "Нужна повторная авторизация"
        DomainError.Forbidden -> "Недостаточно прав"
        DomainError.NotFound -> "Пользователь не найден"
        is DomainError.Network -> "Ошибка сети"
        is DomainError.Unknown -> "Что-то пошло не так"
    }
