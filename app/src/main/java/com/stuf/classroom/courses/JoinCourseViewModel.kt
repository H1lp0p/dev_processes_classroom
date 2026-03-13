package com.stuf.classroom.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stuf.classroom.di.MainDispatcher
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Course
import com.stuf.domain.usecase.JoinCourse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class JoinCourseViewModel @Inject constructor(
    private val joinCourse: JoinCourse,
    @MainDispatcher private val dispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState: MutableStateFlow<JoinCourseUiState> = MutableStateFlow(JoinCourseUiState())
    val uiState: StateFlow<JoinCourseUiState> = _uiState

    fun onInviteCodeChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            inviteCode = value,
            inviteCodeError = null,
            generalError = null,
        )
    }

    fun onJoinClick() {
        val current: JoinCourseUiState = _uiState.value
        val trimmedCode: String = current.inviteCode.trim()

        if (trimmedCode.isBlank()) {
            _uiState.value = current.copy(
                inviteCodeError = "Invite code is required",
            )
            return
        }

        _uiState.value = current.copy(
            isLoading = true,
            inviteCodeError = null,
            generalError = null,
        )

        viewModelScope.launch(dispatcher) {
            when (val result: DomainResult<Course> = joinCourse(trimmedCode)) {
                is DomainResult.Success -> {
                    val course: Course = result.value
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        inviteCodeError = null,
                        generalError = null,
                        isJoined = true,
                        joinedCourseId = course.id,
                    )
                }

                is DomainResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        generalError = result.error.toUserMessage(),
                        isJoined = false,
                    )
                }
            }
        }
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

