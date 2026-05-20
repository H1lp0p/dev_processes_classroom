package com.stuf.classroom.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stuf.classroom.di.MainDispatcher
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.usecase.ChangePassword
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

data class ChangePasswordUiState(
    val oldPassword: String = "",
    val newPassword: String = "",
    val isLoading: Boolean = false,
    val oldPasswordError: String? = null,
    val newPasswordError: String? = null,
    val generalError: String? = null,
)

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val changePassword: ChangePassword,
    @MainDispatcher private val dispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events.asSharedFlow()

    fun onOldPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(oldPassword = value, oldPasswordError = null, generalError = null)
    }

    fun onNewPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(newPassword = value, newPasswordError = null, generalError = null)
    }

    fun onSubmit(onSuccess: () -> Unit) {
        val current = _uiState.value
        var oldPasswordError: String? = null
        var newPasswordError: String? = null
        if (current.oldPassword.isBlank()) oldPasswordError = "Введите старый пароль"
        if (current.newPassword.length < 6) newPasswordError = "Минимум 6 символов"
        if (oldPasswordError != null || newPasswordError != null) {
            _uiState.value = current.copy(oldPasswordError = oldPasswordError, newPasswordError = newPasswordError)
            return
        }

        _uiState.value = current.copy(isLoading = true, generalError = null)
        viewModelScope.launch(dispatcher) {
            when (val result = changePassword(current.oldPassword, current.newPassword)) {
                is DomainResult.Success -> {
                    _uiState.value = ChangePasswordUiState()
                    _events.emit("Пароль успешно изменён")
                    onSuccess()
                }
                is DomainResult.Failure -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, generalError = result.error.toUserMessage())
                    _events.emit(_uiState.value.generalError ?: "Не удалось сменить пароль")
                }
            }
        }
    }
}

private fun DomainError.toUserMessage(): String =
    when (this) {
        is DomainError.Validation -> message
        DomainError.Unauthorized -> "Неверный старый пароль или сессия истекла"
        DomainError.Forbidden -> "Недостаточно прав"
        DomainError.NotFound -> "Пользователь не найден"
        is DomainError.Network -> "Ошибка сети"
        is DomainError.Unknown -> "Что-то пошло не так"
    }
