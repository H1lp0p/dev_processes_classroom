package com.stuf.classroom.post

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.stuf.classroom.ui.theme.ClassroomTheme
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.SolutionId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class PostScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun postScreen_shows_header_and_body_for_post() {
        val state: PostUiState = PostUiState(
            postTitle = "Post title",
            postText = "Post body",
            isTask = false,
            currentUserRole = CourseRole.STUDENT,
        )

        composeRule.setContent {
            ClassroomTheme {
                PostScreen(
                    state = state,
                    onRetry = {},
                    onAttachSolutionClick = {},
                    onCommentSubmit = { _, _, _ -> },
                    onLoadRepliesClick = {},
                    onToggleCommentsVisibility = {},
                    onSolutionClick = {},
                    onBackClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("post_header_card").assertIsDisplayed()
        composeRule.onNodeWithTag("post_title").assertIsDisplayed()
        composeRule.onNodeWithTag("post_text").assertIsDisplayed()
        composeRule.onNodeWithTag("post_type_label").assertIsDisplayed()

        // Для обычного поста кнопка прикрепления решения не отображается
        composeRule.onNodeWithTag("post_attach_solution_button")
            .assertDoesNotExist()
    }

    @Test
    fun postScreen_shows_attach_solution_button_for_task_and_student() {
        val stateStudent: PostUiState = PostUiState(
            postTitle = "Task title",
            postText = "Task body",
            isTask = true,
            currentUserRole = CourseRole.STUDENT,
        )

        composeRule.setContent {
            ClassroomTheme {
                PostScreen(
                    state = stateStudent,
                    onRetry = {},
                    onAttachSolutionClick = {},
                    onCommentSubmit = { _, _, _ -> },
                    onLoadRepliesClick = {},
                    onToggleCommentsVisibility = {},
                    onSolutionClick = {},
                    onBackClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("post_attach_solution_button").assertIsDisplayed()
    }

    @Test
    fun postScreen_hides_attach_solution_button_for_task_and_teacher() {
        val stateTeacher: PostUiState = PostUiState(
            postTitle = "Task title",
            postText = "Task body",
            isTask = true,
            currentUserRole = CourseRole.TEACHER,
        )

        composeRule.setContent {
            ClassroomTheme {
                PostScreen(
                    state = stateTeacher,
                    onRetry = {},
                    onAttachSolutionClick = {},
                    onCommentSubmit = { _, _, _ -> },
                    onLoadRepliesClick = {},
                    onToggleCommentsVisibility = {},
                    onSolutionClick = {},
                    onBackClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("post_attach_solution_button")
            .assertDoesNotExist()
    }

    @Test
    fun postScreen_shows_comments_list() {
        val comments: List<CommentUi> = listOf(
            CommentUi(id = "1", text = "First", authorName = "User 1"),
            CommentUi(id = "2", text = "Second", authorName = "User 2"),
        )
        val state: PostUiState = PostUiState(
            postTitle = "Post",
            postText = "Body",
            isTask = false,
            currentUserRole = CourseRole.STUDENT,
            comments = comments,
        )

        composeRule.setContent {
            ClassroomTheme {
                PostScreen(
                    state = state,
                    onRetry = {},
                    onAttachSolutionClick = {},
                    onCommentSubmit = { _, _, _ -> },
                    onLoadRepliesClick = {},
                    onToggleCommentsVisibility = {},
                    onSolutionClick = {},
                    onBackClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("post_comments_list").assertIsDisplayed()
        composeRule.onAllNodes(hasTestTag("post_comment_item"))
            .assertCountEquals(2)
    }

    @Test
    fun postScreen_allows_adding_public_and_private_comments() {
        var lastText: String? = null
        var lastIsPrivate: Boolean? = null
        var lastParentId: String? = null

        val state: PostUiState = PostUiState(
            postTitle = "Post",
            postText = "Body",
            isTask = false,
            currentUserRole = CourseRole.STUDENT,
        )

        composeRule.setContent {
            ClassroomTheme {
                PostScreen(
                    state = state,
                    onRetry = {},
                    onAttachSolutionClick = {},
                    onCommentSubmit = { text, isPrivate, parentCommentId ->
                        lastText = text
                        lastIsPrivate = isPrivate
                        lastParentId = parentCommentId?.value
                    },
                    onLoadRepliesClick = {},
                    onToggleCommentsVisibility = {},
                    onSolutionClick = {},
                    onBackClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("post_comment_input").performTextInput("Hello")
        composeRule.onNodeWithTag("post_comment_send_button").performClick()

        assertEquals("Hello", lastText)
        assertEquals(false, lastIsPrivate)
        assertEquals(null, lastParentId)
    }

    @Test
    fun postScreen_teacher_can_collapse_comments_and_see_solutions() {
        val state: PostUiState = PostUiState(
            postTitle = "Task",
            postText = "Body",
            isTask = true,
            currentUserRole = CourseRole.TEACHER,
            comments = listOf(
                CommentUi(id = "1", text = "Comment", authorName = "User"),
            ),
            solutions = listOf(
                SolutionUi(
                    id = SolutionId(UUID.randomUUID()),
                    studentName = "Student",
                    status = "Pending",
                ),
            ),
            areCommentsCollapsedForTeacher = false,
        )

        var toggleCalled = false

        composeRule.setContent {
            ClassroomTheme {
                PostScreen(
                    state = state,
                    onRetry = {},
                    onAttachSolutionClick = {},
                    onCommentSubmit = { _, _, _ -> },
                    onLoadRepliesClick = {},
                    onToggleCommentsVisibility = { toggleCalled = true },
                    onSolutionClick = {},
                    onBackClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("post_teacher_toggle_comments_button").assertIsDisplayed()
        composeRule.onNodeWithTag("post_solutions_list").assertIsDisplayed()
        composeRule.onNodeWithTag("post_teacher_toggle_comments_button").performClick()
        assertTrue(toggleCalled)
    }

    @Test
    fun postScreen_teacher_can_open_solution_details() {
        val solutionId: SolutionId = SolutionId(UUID.randomUUID())
        val state: PostUiState = PostUiState(
            postTitle = "Task",
            postText = "Body",
            isTask = true,
            currentUserRole = CourseRole.TEACHER,
            solutions = listOf(
                SolutionUi(
                    id = solutionId,
                    studentName = "Student",
                    status = "Pending",
                ),
            ),
        )

        var lastOpenedSolutionId: SolutionId? = null

        composeRule.setContent {
            ClassroomTheme {
                PostScreen(
                    state = state,
                    onRetry = {},
                    onAttachSolutionClick = {},
                    onCommentSubmit = { _, _, _ -> },
                    onLoadRepliesClick = {},
                    onToggleCommentsVisibility = {},
                    onSolutionClick = { id -> lastOpenedSolutionId = id },
                    onBackClick = {},
                )
            }
        }

        composeRule.onAllNodes(hasTestTag("post_solution_item"))[0].performClick()
        assertEquals(solutionId, lastOpenedSolutionId)
    }

    @Test
    fun postScreen_shows_loading_and_error() {
        val state: PostUiState = PostUiState(
            postTitle = "Post",
            postText = "Body",
            isTask = false,
            currentUserRole = CourseRole.STUDENT,
            isLoading = true,
            error = "Error",
        )

        var retryCalled = false

        composeRule.setContent {
            ClassroomTheme {
                PostScreen(
                    state = state,
                    onRetry = { retryCalled = true },
                    onAttachSolutionClick = {},
                    onCommentSubmit = { _, _, _ -> },
                    onLoadRepliesClick = {},
                    onToggleCommentsVisibility = {},
                    onSolutionClick = {},
                    onBackClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("post_loading_indicator")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("post_error")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("post_retry_button")
            .performClick()

        assertTrue(retryCalled)
    }
}

