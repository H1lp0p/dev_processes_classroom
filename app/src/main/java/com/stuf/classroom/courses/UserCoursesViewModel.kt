package com.stuf.classroom.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stuf.classroom.di.MainDispatcher
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.usecase.GetUserCourses
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserCoursesViewModel @Inject constructor(
    private val getUserCourses: GetUserCourses,
    @MainDispatcher private val dispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserCoursesUiState(isLoading = true))
    val uiState: StateFlow<UserCoursesUiState> = _uiState

    fun onRetry() {
        loadCourses()
    }

    private fun loadCourses() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch(dispatcher) {
            when (val result = getUserCourses()) {
                is DomainResult.Success -> {
                    _uiState.value = UserCoursesUiState(
                        courses = result.value,
                        isLoading = false,
                        error = null,
                    )
                }
                is DomainResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.error.toUserMessage(),
                    )
                }
            }
        }
    }

    private fun DomainError.toUserMessage(): String =
        when (this) {
            is DomainError.Validation -> message ?: "Validation error"
            DomainError.Unauthorized -> "Unauthorized"
            is DomainError.Network -> cause?.message?.let { "Network error: $it" } ?: "Network error"
            is DomainError.Unknown -> cause?.message?.let { "Error: $it" } ?: "Unknown error"
            else -> "Unknown error"
        }
}
