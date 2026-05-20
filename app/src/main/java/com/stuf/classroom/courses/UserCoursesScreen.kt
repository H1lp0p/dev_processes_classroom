package com.stuf.classroom.courses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.stuf.classroom.courses.components.UserCourseItem
import com.stuf.domain.model.UserCourse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserCoursesScreen(
    state: UserCoursesUiState,
    onRetry: () -> Unit,
    onNewCourse: () -> Unit,
    onJoinCourse: () -> Unit,
    onProfile: (() -> Unit)? = null,
    onCourseClick: (UserCourse) -> Unit = {},
) {
    var fabExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои курсы") },
                actions = {
                    if (onProfile != null) {
                        TextButton(onClick = onProfile) {
                            Text("Профиль")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick = { fabExpanded = true },
                    modifier = Modifier.testTag("user_courses_fab"),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
                androidx.compose.material3.DropdownMenu(
                    expanded = fabExpanded,
                    onDismissRequest = { fabExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Новый курс") },
                        onClick = {
                            onNewCourse()
                            fabExpanded = false
                        },
                        modifier = Modifier.testTag("user_courses_new_course"),
                    )
                    DropdownMenuItem(
                        text = { Text("Присоединиться") },
                        onClick = {
                            onJoinCourse()
                            fabExpanded = false
                        },
                        modifier = Modifier.testTag("user_courses_join"),
                    )
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag("user_courses_loading"),
                )
            }
            if (state.error != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = state.error,
                        modifier = Modifier.testTag("user_courses_error"),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    TextButton(
                        onClick = onRetry,
                        modifier = Modifier.testTag("user_courses_retry"),
                    ) {
                        Text("Повторить")
                    }
                }
            }
            if (!state.isLoading && state.error == null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("user_courses_list"),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(state.courses) { index, course ->
                        UserCourseItem(
                            course = course,
                            modifier = Modifier
                                .testTag("user_courses_item_$index")
                                .clickable { onCourseClick(course) },
                        )
                    }
                }
            }
        }
    }
}
