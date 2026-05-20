package com.stuf.classroom.course.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.stuf.classroom.course.CourseScreenUiState
import com.stuf.domain.model.CourseMember
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.UserId

@Composable
internal fun CourseMembersTabContent(
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

                        val isCurrentUser: Boolean =
                            state.currentUserId != null && member.id == state.currentUserId

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
