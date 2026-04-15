package com.stuf.classroom.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stuf.classroom.di.MainDispatcher
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.User
import com.stuf.domain.repository.CurrentUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: User? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val currentUserRepository: CurrentUserRepository,
    @MainDispatcher private val dispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun onRetry() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch(dispatcher) {
            when (val result = currentUserRepository.getCurrentUser()) {
                is DomainResult.Success ->
                    _uiState.value =
                        ProfileUiState(
                            isLoading = false,
                            user = result.value,
                        )
                is DomainResult.Failure ->
                    _uiState.value =
                        ProfileUiState(
                            isLoading = false,
                            error = result.error.toUserMessage(),
                        )
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
