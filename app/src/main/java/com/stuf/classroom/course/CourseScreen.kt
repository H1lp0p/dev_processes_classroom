package com.stuf.classroom.course

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.stuf.domain.model.CourseMember
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostId
import com.stuf.domain.model.PostKind
import com.stuf.domain.model.UserId

@Composable
fun CourseScreen(
    state: CourseScreenUiState,
    onTabSelected: (CourseTab) -> Unit,
    onPostClick: (PostId) -> Unit,
    onCreatePostClick: () -> Unit,
    onMemberRoleToggleClick: (UserId) -> Unit,
    onMemberRemoveClick: (UserId) -> Unit,
    onBackClick: () -> Unit,
    onLeaveCourseClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Кнопка выхода в правом верхнем углу
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                )
            }

            TextButton(
                onClick = onLeaveCourseClick,
                modifier = Modifier.testTag("course_leave_button"),
            ) {
                Text("Покинуть курс")
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            val effectiveTab: CourseTab =
                if (state.currentUserRole == CourseRole.TEACHER) {
                    state.selectedTab
                } else {
                    CourseTab.COURSE
                }

            when (effectiveTab) {
                CourseTab.COURSE -> CourseTabContent(
                    state = state,
                    onPostClick = onPostClick,
                    onCreatePostClick = onCreatePostClick,
                )

                CourseTab.MEMBERS -> MembersTabContent(
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

        NavigationBar {
            NavigationBarItem(
                selected = state.selectedTab == CourseTab.COURSE,
                onClick = { onTabSelected(CourseTab.COURSE) },
                icon = { },
                label = { Text("Курс") },
                modifier = Modifier.testTag("course_tab_course"),
            )

            if (state.currentUserRole == CourseRole.TEACHER) {
                NavigationBarItem(
                    selected = state.selectedTab == CourseTab.MEMBERS,
                    onClick = { onTabSelected(CourseTab.MEMBERS) },
                    icon = { },
                    label = { Text("Пользователи") },
                    modifier = Modifier.testTag("course_tab_members"),
                )
            }
        }
    }
}

@Composable
private fun CourseTabContent(
    state: CourseScreenUiState,
    onPostClick: (PostId) -> Unit,
    onCreatePostClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("course_header_card"),
            colors = CardDefaults.cardColors(),
            elevation = CardDefaults.cardElevation(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = state.courseTitle,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.testTag("course_title"),
                )
                state.inviteCode?.let { code ->
                    Text(
                        text = "Код: $code",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.testTag("course_invite_code"),
                    )
                }
                state.currentUserRole?.let { role ->
                    val roleLabel: String = when (role) {
                        CourseRole.TEACHER -> "Учитель"
                        CourseRole.STUDENT -> "Ученик"
                    }
                    Text(
                        text = roleLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.testTag("course_user_role"),
                    )
                }
            }
        }

        Button(
            onClick = onCreatePostClick,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("course_create_post_button"),
        ) {
            Text("Создать пост")
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("course_posts_list"),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.posts) { post: Post ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPostClick(post.id) }
                        .testTag("course_post_item"),
                    colors = CardDefaults.cardColors(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = post.title,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        val typeLabel: String = when (post.kind) {
                            PostKind.ANNOUNCEMENT -> "Пост"
                            PostKind.MATERIAL -> "Материал"
                            PostKind.TASK -> "Задача"
                        }
                        Text(
                            text = typeLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MembersTabContent(
    state: CourseScreenUiState,
    onMemberRoleToggleClick: (UserId) -> Unit,
    onMemberRemoveClick: (UserId) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("course_members_list"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.members) { member: CourseMember ->
                val isMenuExpanded: MutableState<Boolean> = remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("course_member_item")
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        val isCreator: Boolean = member.id == state.courseAuthorId

                        // TODO
                        val isCurrentUser: Boolean = false

                        val suffixes = buildList {
                            if (isCreator) add("[Создатель]")
                            if (isCurrentUser) add("[Вы]")
                        }
                        val nameText: String =
                            if (suffixes.isNotEmpty()) {
                                member.credentials + " " + suffixes.joinToString(" ")
                            } else {
                                member.credentials
                            }

                        Text(
                            text = nameText,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        val roleLabel: String = when (member.role) {
                            CourseRole.TEACHER -> "Учитель"
                            CourseRole.STUDENT -> "Студент"
                        }
                        Text(
                            text = roleLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.testTag("course_member_role"),
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (state.currentUserRole == CourseRole.TEACHER &&
                            member.id != state.courseAuthorId
                        ) {
                            IconButton(
                                onClick = { isMenuExpanded.value = true },
                                modifier = Modifier.testTag("course_member_menu_button"),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = null,
                                )
                            }
                            DropdownMenu(
                                expanded = isMenuExpanded.value,
                                onDismissRequest = { isMenuExpanded.value = false },
                            ) {
                                val roleActionText: String = when (member.role) {
                                    CourseRole.TEACHER -> "Сделать учеником"
                                    CourseRole.STUDENT -> "Назначить учителем"
                                }
                                DropdownMenuItem(
                                    text = { Text(roleActionText) },
                                    onClick = {
                                        isMenuExpanded.value = false
                                        onMemberRoleToggleClick(member.id)
                                    },
                                    modifier = Modifier.testTag("course_member_toggle_role_menu_item"),
                                )
                                DropdownMenuItem(
                                    text = { Text("Удалить из курса") },
                                    onClick = {
                                        isMenuExpanded.value = false
                                        onMemberRemoveClick(member.id)
                                    },
                                    modifier = Modifier.testTag("course_member_remove_menu_item"),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

