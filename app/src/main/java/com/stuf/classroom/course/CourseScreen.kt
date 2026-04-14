package com.stuf.classroom.course

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.stuf.classroom.course.components.CourseCourseTabContent
import com.stuf.classroom.course.components.CourseMembersTabContent
import com.stuf.classroom.course.components.CourseScreenTopBar
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.PostId
import com.stuf.domain.model.UserId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScreen(
    state: CourseScreenUiState,
    onTabSelected: (CourseTab) -> Unit,
    onPostClick: (PostId) -> Unit,
    onMemberRoleToggleClick: (UserId) -> Unit,
    onMemberRemoveClick: (UserId) -> Unit,
    onBackClick: () -> Unit,
    onLeaveCourseClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CourseScreenTopBar(
                onBackClick = onBackClick,
                onLeaveCourseClick = onLeaveCourseClick,
            )

            if (state.currentUserRole == CourseRole.TEACHER) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = state.selectedTab == CourseTab.COURSE,
                        onClick = { onTabSelected(CourseTab.COURSE) },
                        label = { Text("Лента") },
                        modifier = Modifier.testTag("course_tab_course"),
                    )
                    FilterChip(
                        selected = state.selectedTab == CourseTab.MEMBERS,
                        onClick = { onTabSelected(CourseTab.MEMBERS) },
                        label = { Text("Пользователи") },
                        modifier = Modifier.testTag("course_tab_members"),
                    )
                }
            }

            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
            ) {
            val effectiveTab: CourseTab =
                if (state.currentUserRole == CourseRole.TEACHER) {
                    state.selectedTab
                } else {
                    CourseTab.COURSE
                }

            when (effectiveTab) {
                CourseTab.COURSE -> CourseCourseTabContent(
                    state = state,
                    onPostClick = onPostClick,
                )

                CourseTab.MEMBERS -> CourseMembersTabContent(
                    state = state,
                    onMemberRoleToggleClick = onMemberRoleToggleClick,
                    onMemberRemoveClick = onMemberRemoveClick,
                )
            }

            if (state.isLoadingCourse) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag("course_loading_course_indicator"),
                )
            }
            if (state.isLoadingFeed) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag("course_loading_feed_indicator"),
                )
            }
            if (state.isLoadingMembers) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag("course_loading_members_indicator"),
                )
            }

            if (state.error != null) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(8.dp)
                        .testTag("course_error"),
                )
            }
        }
        }
    }
}
