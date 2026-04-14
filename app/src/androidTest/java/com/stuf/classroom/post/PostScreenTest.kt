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
import com.stuf.domain.model.AnnouncementPost
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.PostId
import com.stuf.domain.model.TaskDetails
import com.stuf.domain.model.TaskPost
import java.time.OffsetDateTime
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PostScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val cid: CourseId = CourseId(UUID.randomUUID())
    private val pid: PostId = PostId(UUID.randomUUID())
    private val t: OffsetDateTime = OffsetDateTime.now()

    @Test
    fun postScreen_shows_header_and_body_for_post() {
        val state: PostUiState =
            PostUiState(
                content =
                    PostScreenContent.Announcement(
                        AnnouncementPost(
                            id = pid,
                            courseId = cid,
                            title = "Post title",
                            text = "Post body",
                            createdAt = t,
                        ),
                    ),
                currentUserRole = CourseRole.STUDENT,
            )

        composeRule.setContent {
            ClassroomTheme {
                PostScreen(
                    state = state,
                    onRetry = {},
                    onPickSolutionFile = {},
                    onSubmitTeamSolution = {},
                    onSubmitIndividualSolution = {},
                    onRemovePendingTeamSolutionFile = {},
                    onRemovePendingIndividualSolutionFile = {},
                    onRemoveSavedTeamSolutionFile = {},
                    onRemoveSavedIndividualSolutionFile = {},
                    onJoinTeam = {},
                    onLeaveTeam = {},
                    onCommentSubmit = { _, _, _ -> },
                    onLoadRepliesClick = {},
                    onBackClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("post_header_card").assertIsDisplayed()
        composeRule.onNodeWithTag("post_title").assertIsDisplayed()
        composeRule.onNodeWithTag("post_text").assertIsDisplayed()
        composeRule.onNodeWithTag("post_type_label").assertIsDisplayed()

        composeRule.onNodeWithTag("post_attach_solution_button")
            .assertDoesNotExist()
    }

    @Test
    fun postScreen_shows_individual_task_main_for_task_and_student() {
        val stateStudent: PostUiState =
            PostUiState(
                content =
                    PostScreenContent.Task(
                        TaskPost(
                            id = pid,
                            courseId = cid,
                            title = "Task title",
                            text = "Task body",
                            createdAt = t,
                            taskDetails =
                                TaskDetails(
                                    deadline = null,
                                    isMandatory = true,
                                    maxScore = 5,
                                ),
                        ),
                    ),
                currentUserRole = CourseRole.STUDENT,
            )

        composeRule.setContent {
            ClassroomTheme {
                PostScreen(
                    state = stateStudent,
                    onRetry = {},
                    onPickSolutionFile = {},
                    onSubmitTeamSolution = {},
                    onSubmitIndividualSolution = {},
                    onRemovePendingTeamSolutionFile = {},
                    onRemovePendingIndividualSolutionFile = {},
                    onRemoveSavedTeamSolutionFile = {},
                    onRemoveSavedIndividualSolutionFile = {},
                    onJoinTeam = {},
                    onLeaveTeam = {},
                    onCommentSubmit = { _, _, _ -> },
                    onLoadRepliesClick = {},
                    onBackClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("post_attach_solution_button")
            .assertDoesNotExist()
        composeRule.onNodeWithTag("individual_task_post_main").assertIsDisplayed()
    }

    @Test
    fun postScreen_shows_individual_task_main_for_task_and_teacher() {
        val stateTeacher: PostUiState =
            PostUiState(
                content =
                    PostScreenContent.Task(
                        TaskPost(
                            id = pid,
                            courseId = cid,
                            title = "Task title",
                            text = "Task body",
                            createdAt = t,
                            taskDetails =
                                TaskDetails(
                                    deadline = null,
                                    isMandatory = true,
                                    maxScore = 5,
                                ),
                        ),
                    ),
                currentUserRole = CourseRole.TEACHER,
            )

        composeRule.setContent {
            ClassroomTheme {
                PostScreen(
                    state = stateTeacher,
                    onRetry = {},
                    onPickSolutionFile = {},
                    onSubmitTeamSolution = {},
                    onSubmitIndividualSolution = {},
                    onRemovePendingTeamSolutionFile = {},
                    onRemovePendingIndividualSolutionFile = {},
                    onRemoveSavedTeamSolutionFile = {},
                    onRemoveSavedIndividualSolutionFile = {},
                    onJoinTeam = {},
                    onLeaveTeam = {},
                    onCommentSubmit = { _, _, _ -> },
                    onLoadRepliesClick = {},
                    onBackClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("post_attach_solution_button")
            .assertDoesNotExist()
        composeRule.onNodeWithTag("individual_task_post_main").assertIsDisplayed()
    }

    @Test
    fun postScreen_shows_comments_list() {
        val comments: List<CommentUi> = listOf(
            CommentUi(
                id = "1",
                text = "First",
                authorName = "User 1",
                createdAtLabel = "1 янв., 12:00",
            ),
            CommentUi(
                id = "2",
                text = "Second",
                authorName = "User 2",
                createdAtLabel = "1 янв., 12:00",
            ),
        )
        val state: PostUiState =
            PostUiState(
                content =
                    PostScreenContent.Announcement(
                        AnnouncementPost(
                            id = pid,
                            courseId = cid,
                            title = "Post",
                            text = "Body",
                            createdAt = t,
                        ),
                    ),
                currentUserRole = CourseRole.STUDENT,
                comments = comments,
            )

        composeRule.setContent {
            ClassroomTheme {
                PostScreen(
                    state = state,
                    onRetry = {},
                    onPickSolutionFile = {},
                    onSubmitTeamSolution = {},
                    onSubmitIndividualSolution = {},
                    onRemovePendingTeamSolutionFile = {},
                    onRemovePendingIndividualSolutionFile = {},
                    onRemoveSavedTeamSolutionFile = {},
                    onRemoveSavedIndividualSolutionFile = {},
                    onJoinTeam = {},
                    onLeaveTeam = {},
                    onCommentSubmit = { _, _, _ -> },
                    onLoadRepliesClick = {},
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

        val state: PostUiState =
            PostUiState(
                content =
                    PostScreenContent.Announcement(
                        AnnouncementPost(
                            id = pid,
                            courseId = cid,
                            title = "Post",
                            text = "Body",
                            createdAt = t,
                        ),
                    ),
                currentUserRole = CourseRole.STUDENT,
            )

        composeRule.setContent {
            ClassroomTheme {
                PostScreen(
                    state = state,
                    onRetry = {},
                    onPickSolutionFile = {},
                    onSubmitTeamSolution = {},
                    onSubmitIndividualSolution = {},
                    onRemovePendingTeamSolutionFile = {},
                    onRemovePendingIndividualSolutionFile = {},
                    onRemoveSavedTeamSolutionFile = {},
                    onRemoveSavedIndividualSolutionFile = {},
                    onJoinTeam = {},
                    onLeaveTeam = {},
                    onCommentSubmit = { text, isPrivate, parentCommentId ->
                        lastText = text
                        lastIsPrivate = isPrivate
                        lastParentId = parentCommentId?.value
                    },
                    onLoadRepliesClick = {},
                    onBackClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("post_new_comment_button").performClick()
        composeRule.onNodeWithTag("post_comment_input").performTextInput("Hello")
        composeRule.onNodeWithTag("post_comment_send_button").performClick()

        assertEquals("Hello", lastText)
        assertEquals(false, lastIsPrivate)
        assertEquals(null, lastParentId)
    }

    @Test
    fun postScreen_shows_loading_and_error() {
        var retryCalled = false

        composeRule.setContent {
            ClassroomTheme {
                PostScreen(
                    state =
                        PostUiState(
                            isLoadingPost = true,
                            content = null,
                            currentUserRole = CourseRole.STUDENT,
                        ),
                    onRetry = {},
                    onPickSolutionFile = {},
                    onSubmitTeamSolution = {},
                    onSubmitIndividualSolution = {},
                    onRemovePendingTeamSolutionFile = {},
                    onRemovePendingIndividualSolutionFile = {},
                    onRemoveSavedTeamSolutionFile = {},
                    onRemoveSavedIndividualSolutionFile = {},
                    onJoinTeam = {},
                    onLeaveTeam = {},
                    onCommentSubmit = { _, _, _ -> },
                    onLoadRepliesClick = {},
                    onBackClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("post_loading_indicator")
            .assertIsDisplayed()

        composeRule.setContent {
            ClassroomTheme {
                PostScreen(
                    state =
                        PostUiState(
                            isLoadingPost = false,
                            postLoadError = "Error",
                            content = null,
                            currentUserRole = CourseRole.STUDENT,
                        ),
                    onRetry = { retryCalled = true },
                    onPickSolutionFile = {},
                    onSubmitTeamSolution = {},
                    onSubmitIndividualSolution = {},
                    onRemovePendingTeamSolutionFile = {},
                    onRemovePendingIndividualSolutionFile = {},
                    onRemoveSavedTeamSolutionFile = {},
                    onRemoveSavedIndividualSolutionFile = {},
                    onJoinTeam = {},
                    onLeaveTeam = {},
                    onCommentSubmit = { _, _, _ -> },
                    onLoadRepliesClick = {},
                    onBackClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("post_error")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("post_retry_button")
            .performClick()

        assertTrue(retryCalled)
    }
}
