package com.stuf.classroom.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stuf.classroom.di.MainDispatcher
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Course
import com.stuf.domain.usecase.CreateCourse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CreateCourseViewModel @Inject constructor(
    private val createCourse: CreateCourse,
    @MainDispatcher private val dispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState: MutableStateFlow<CreateCourseUiState> = MutableStateFlow(CreateCourseUiState())
    val uiState: StateFlow<CreateCourseUiState> = _uiState

    fun onTitleChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            title = value,
            titleError = null,
            generalError = null,
        )
    }

    fun onCreateClick() {
        val current: CreateCourseUiState = _uiState.value
        val trimmedTitle: String = current.title.trim()

        if (trimmedTitle.isBlank()) {
            _uiState.value = current.copy(
                titleError = "Title is required",
            )
            return
        }

        _uiState.value = current.copy(
            isLoading = true,
            titleError = null,
            generalError = null,
        )

        viewModelScope.launch(dispatcher) {
            when (val result: DomainResult<Course> = createCourse(trimmedTitle)) {
                is DomainResult.Success -> {
                    val course: Course = result.value
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        titleError = null,
                        generalError = null,
                        isCreated = true,
                        createdCourseId = course.id,
                    )
                }

                is DomainResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        generalError = result.error.toUserMessage(),
                        isCreated = false,
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

