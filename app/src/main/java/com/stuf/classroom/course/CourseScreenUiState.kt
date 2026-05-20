package com.stuf.classroom.course

import com.stuf.domain.model.CourseId
import com.stuf.domain.model.CourseMember
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.Post
import com.stuf.domain.model.UserId

data class CourseScreenUiState(
    val courseId: CourseId,
    val courseTitle: String = "",
    val inviteCode: String? = null,
    val currentUserRole: CourseRole? = null,
    val courseAuthorId: UserId? = null,
    val currentUserId: UserId? = null,
    val selectedTab: CourseTab = CourseTab.COURSE,
    val posts: List<Post> = emptyList(),
    val members: List<CourseMember> = emptyList(),
    val isLoadingCourse: Boolean = false,
    val isLoadingFeed: Boolean = false,
    val isLoadingMembers: Boolean = false,
    val error: String? = null,
)
