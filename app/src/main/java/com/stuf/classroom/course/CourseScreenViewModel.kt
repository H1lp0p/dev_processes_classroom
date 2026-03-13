package com.stuf.classroom.course

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stuf.classroom.di.MainDispatcher
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Course
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.CourseMember
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostId
import com.stuf.domain.model.UserId
import com.stuf.domain.usecase.ChangeMemberRole
import com.stuf.domain.usecase.GetCourseFeed
import com.stuf.domain.usecase.GetCourseInfo
import com.stuf.domain.usecase.GetCourseMembers
import com.stuf.domain.usecase.LeaveCourse
import com.stuf.domain.usecase.RemoveMember
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

enum class CourseTab {
    COURSE,
    MEMBERS,
}

data class CourseScreenUiState(
    val courseId: CourseId,
    val courseTitle: String = "",
    val inviteCode: String? = null,
    val currentUserRole: CourseRole? = null,
    val courseAuthorId: UserId? = null,
    val selectedTab: CourseTab = CourseTab.COURSE,
    val posts: List<Post> = emptyList(),
    val members: List<CourseMember> = emptyList(),
    val isLoadingCourse: Boolean = false,
    val isLoadingFeed: Boolean = false,
    val isLoadingMembers: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class CourseScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCourseInfo: GetCourseInfo,
    private val getCourseFeed: GetCourseFeed,
    private val getCourseMembers: GetCourseMembers,
    private val changeMemberRole: ChangeMemberRole,
    private val removeMember: RemoveMember,
    private val leaveCourse: LeaveCourse,
    @MainDispatcher private val dispatcher: CoroutineDispatcher,
) : ViewModel() {

    val courseId: CourseId =
        CourseId(UUID.fromString(requireNotNull(savedStateHandle.get<String>("courseId"))))

    private val initialRole: CourseRole? = when (savedStateHandle.get<String>("role")?.lowercase()) {
        "teacher" -> CourseRole.TEACHER
        "student" -> CourseRole.STUDENT
        else -> null
    }

    private val _uiState: MutableStateFlow<CourseScreenUiState> =
        MutableStateFlow(
            CourseScreenUiState(
                courseId = courseId,
                currentUserRole = initialRole,
            ),
        )
    val uiState: StateFlow<CourseScreenUiState> = _uiState.asStateFlow()

    fun onRetry() {
        viewModelScope.launch(dispatcher) {
            _uiState.value = _uiState.value.copy(
                isLoadingCourse = true,
                isLoadingFeed = true,
                isLoadingMembers = true,
                error = null,
            )

            val courseResult: DomainResult<Course> = getCourseInfo(courseId)
            val feedResult: DomainResult<List<Post>> = getCourseFeed(courseId)
            val membersResult: DomainResult<List<CourseMember>> = getCourseMembers(courseId, null)

            var newState = _uiState.value.copy(
                isLoadingCourse = false,
                isLoadingFeed = false,
                isLoadingMembers = false,
            )

            val courseError: String? = when (courseResult) {
                is DomainResult.Success -> {
                    newState = newState.copy(
                        courseTitle = courseResult.value.title,
                        inviteCode = courseResult.value.inviteCode,
                        courseAuthorId = courseResult.value.authorId,
                    )
                    null
                }
                is DomainResult.Failure -> mapError(courseResult.error)
            }

            val feedError: String? = when (feedResult) {
                is DomainResult.Success -> {
                    newState = newState.copy(posts = feedResult.value)
                    null
                }
                is DomainResult.Failure -> mapError(feedResult.error)
            }

            val membersError: String? = when (membersResult) {
                is DomainResult.Success -> {
                    newState = newState.copy(members = membersResult.value)
                    null
                }
                is DomainResult.Failure -> mapError(membersResult.error)
            }

            _uiState.value = newState.copy(
                error = courseError ?: feedError ?: membersError,
            )
        }
    }

    fun onRemoveMemberClick(userId: UserId) {
        viewModelScope.launch(dispatcher) {
            val result: DomainResult<Unit> = removeMember(courseId, userId)
            when (result) {
                is DomainResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        members = _uiState.value.members.filterNot { it.id == userId },
                    )
                }

                is DomainResult.Failure -> {
                    _uiState.value = _uiState.value.copy(error = mapError(result.error))
                }
            }
        }
    }

    fun onChangeMemberRoleClick(userId: UserId) {
        viewModelScope.launch(dispatcher) {
            val currentMembers: List<CourseMember> = _uiState.value.members
            val member: CourseMember = currentMembers.firstOrNull { it.id == userId } ?: return@launch

            val newRole: CourseRole = when (member.role) {
                CourseRole.TEACHER -> CourseRole.STUDENT
                CourseRole.STUDENT -> CourseRole.TEACHER
            }

            val result: DomainResult<Unit> = changeMemberRole(courseId, userId, newRole)
            when (result) {
                is DomainResult.Success -> {
                    val updatedMembers: List<CourseMember> = currentMembers.map {
                        if (it.id == userId) {
                            it.copy(role = newRole)
                        } else {
                            it
                        }
                    }
                    _uiState.value = _uiState.value.copy(members = updatedMembers)
                }

                is DomainResult.Failure -> {
                    _uiState.value = _uiState.value.copy(error = mapError(result.error))
                }
            }
        }
    }

    fun onLeaveCourseClick() {
        viewModelScope.launch(dispatcher) {
            val result: DomainResult<Unit> = leaveCourse(courseId)
            if (result is DomainResult.Failure) {
                _uiState.value = _uiState.value.copy(error = mapError(result.error))
            }
        }
    }

    fun onTabSelected(tab: CourseTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    private suspend fun mapError(error: DomainError): String =
        withContext(dispatcher) {
            when (error) {
                DomainError.Unauthorized -> "Не авторизован"
                DomainError.Forbidden -> "Доступ запрещён"
                DomainError.NotFound -> "Не найдено"
                is DomainError.Validation -> error.message
                is DomainError.Network -> "Ошибка сети"
                is DomainError.Unknown -> "Неизвестная ошибка"
            }.also {
                if (error is DomainError.Forbidden) {
                    Log.e("CourseScreenViewModel", error.toString())
                }
            }
        }
}

