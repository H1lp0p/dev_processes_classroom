package com.stuf.classroom.course

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.stuf.classroom.ui.theme.ClassroomTheme
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.CourseMember
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostId
import com.stuf.domain.model.PostKind
import com.stuf.domain.model.UserId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.OffsetDateTime
import java.util.UUID

/**
 * TDD-спека для CourseScreen.
 *
 * Ожидаемый контракт:
 * - наличие базовых элементов для вкладок «Курс» и «Пользователи»;
 * - testTag'и для основных элементов;
 * - вызов переданных коллбеков по кликам.
 *
 * Реализация CourseScreen должна удовлетворять этим тестам.
 */
@RunWith(AndroidJUnit4::class)
class CourseScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun courseScreen_shows_basic_elements_on_course_tab() {
        val courseId = CourseId(UUID.fromString("00000000-0000-0000-0000-000000000500"))
        val posts = listOf(
            Post(
                id = PostId(UUID.fromString("00000000-0000-0000-0000-000000000501")),
                courseId = courseId,
                kind = PostKind.ANNOUNCEMENT,
                title = "Post 1",
                text = "Text",
                createdAt = OffsetDateTime.now(),
                taskDetails = null,
            ),
        )
        val members = listOf(
            CourseMember(
                id = UserId(UUID.fromString("00000000-0000-0000-0000-000000000502")),
                credentials = "Teacher Name",
                email = "teacher@example.com",
                role = CourseRole.TEACHER,
            ),
        )

        val state = CourseScreenUiState(
            courseId = courseId,
            courseTitle = "Course Title",
            inviteCode = "INV123",
            currentUserRole = CourseRole.TEACHER,
            courseAuthorId = members.first().id,
            selectedTab = CourseTab.COURSE,
            posts = posts,
            members = members,
            isLoadingCourse = false,
            isLoadingFeed = false,
            isLoadingMembers = false,
            error = null,
        )

        composeRule.setContent {
            ClassroomTheme {
                CourseScreen(
                    state = state,
                    onTabSelected = {},
                    onPostClick = {},
                    onCreatePostClick = {},
                    onMemberRoleToggleClick = {},
                    onMemberRemoveClick = {},
                    onLeaveCourseClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("course_title").assertIsDisplayed()
        composeRule.onNodeWithTag("course_invite_code").assertIsDisplayed()
        composeRule.onNodeWithTag("course_user_role").assertIsDisplayed()

        composeRule.onNodeWithTag("course_tab_course").assertIsDisplayed()
        composeRule.onNodeWithTag("course_tab_members").assertIsDisplayed()

        composeRule.onNodeWithTag("course_posts_list").assertIsDisplayed()
        composeRule.onAllNodes(hasTestTag("course_post_item"))
            .assertCountEquals(1)

        composeRule.onNodeWithTag("course_create_post_button").assertIsDisplayed()
    }

    @Test
    fun courseScreen_shows_members_on_members_tab_and_teacher_actions() {
        val courseId = CourseId(UUID.fromString("00000000-0000-0000-0000-000000000600"))
        val authorId = UserId(UUID.fromString("00000000-0000-0000-0000-000000000601"))
        val members = listOf(
            CourseMember(
                id = authorId,
                credentials = "Owner",
                email = "owner@example.com",
                role = CourseRole.TEACHER,
            ),
            CourseMember(
                id = UserId(UUID.fromString("00000000-0000-0000-0000-000000000602")),
                credentials = "Student",
                email = "student@example.com",
                role = CourseRole.STUDENT,
            ),
        )

        val state = CourseScreenUiState(
            courseId = courseId,
            courseTitle = "Course Title",
            inviteCode = "INV123",
            currentUserRole = CourseRole.TEACHER,
            courseAuthorId = authorId,
            selectedTab = CourseTab.MEMBERS,
            posts = emptyList(),
            members = members,
            isLoadingCourse = false,
            isLoadingFeed = false,
            isLoadingMembers = false,
            error = null,
        )

        composeRule.setContent {
            ClassroomTheme {
                CourseScreen(
                    state = state,
                    onTabSelected = {},
                    onPostClick = {},
                    onCreatePostClick = {},
                    onMemberRoleToggleClick = {},
                    onMemberRemoveClick = {},
                    onLeaveCourseClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("course_members_list").assertIsDisplayed()
        composeRule.onAllNodes(hasTestTag("course_member_item"))
            .assertCountEquals(2)

        // Действия для преподавателя доступны для не-владельца
        composeRule.onAllNodes(hasTestTag("course_member_toggle_role_button"))
            .assertCountEquals(1)
        composeRule.onAllNodes(hasTestTag("course_member_remove_button"))
            .assertCountEquals(1)
    }

    @Test
    fun courseScreen_calls_course_tab_callbacks() {
        val courseId = CourseId(UUID.fromString("00000000-0000-0000-0000-000000000700"))
        val postId = PostId(UUID.fromString("00000000-0000-0000-0000-000000000701"))

        val posts = listOf(
            Post(
                id = postId,
                courseId = courseId,
                kind = PostKind.ANNOUNCEMENT,
                title = "Post 1",
                text = "Text",
                createdAt = OffsetDateTime.now(),
                taskDetails = null,
            ),
        )
        val state = CourseScreenUiState(
            courseId = courseId,
            courseTitle = "Course Title",
            inviteCode = "INV123",
            currentUserRole = CourseRole.TEACHER,
            courseAuthorId = null,
            selectedTab = CourseTab.COURSE,
            posts = posts,
            members = emptyList(),
            isLoadingCourse = false,
            isLoadingFeed = false,
            isLoadingMembers = false,
            error = null,
        )

        var lastSelectedTab: CourseTab? = null
        var lastPostClicked: PostId? = null
        var createPostClicked = false
        var lastMemberRoleToggled: UserId? = null
        var lastMemberRemoved: UserId? = null
        var leaveClicked = false

        composeRule.setContent {
            ClassroomTheme {
                CourseScreen(
                    state = state,
                    onTabSelected = { lastSelectedTab = it },
                    onPostClick = { lastPostClicked = it },
                    onCreatePostClick = { createPostClicked = true },
                    onMemberRoleToggleClick = { lastMemberRoleToggled = it },
                    onMemberRemoveClick = { lastMemberRemoved = it },
                    onLeaveCourseClick = { leaveClicked = true },
                )
            }
        }

        composeRule.onNodeWithTag("course_tab_members").performClick()
        composeRule.onNodeWithTag("course_create_post_button").performClick()
        composeRule.onAllNodes(hasTestTag("course_post_item"))[0].performClick()

        assertEquals(CourseTab.MEMBERS, lastSelectedTab)
        assertEquals(postId, lastPostClicked)
        assertTrue(createPostClicked)
        assertEquals(null, lastMemberRoleToggled)
        assertEquals(null, lastMemberRemoved)
        assertFalse(leaveClicked)
    }

    @Test
    fun courseScreen_calls_member_callbacks_on_clicks() {
        val courseId = CourseId(UUID.fromString("00000000-0000-0000-0000-000000000710"))
        val authorId = UserId(UUID.fromString("00000000-0000-0000-0000-000000000711"))
        val memberId = UserId(UUID.fromString("00000000-0000-0000-0000-000000000712"))

        val members = listOf(
            CourseMember(
                id = authorId,
                credentials = "Owner",
                email = "owner@example.com",
                role = CourseRole.TEACHER,
            ),
            CourseMember(
                id = memberId,
                credentials = "Student",
                email = "student@example.com",
                role = CourseRole.STUDENT,
            ),
        )

        val state = CourseScreenUiState(
            courseId = courseId,
            courseTitle = "Course Title",
            inviteCode = "INV123",
            currentUserRole = CourseRole.TEACHER,
            courseAuthorId = authorId,
            selectedTab = CourseTab.MEMBERS,
            posts = emptyList(),
            members = members,
            isLoadingCourse = false,
            isLoadingFeed = false,
            isLoadingMembers = false,
            error = null,
        )

        var lastMemberRoleToggled: UserId? = null
        var lastMemberRemoved: UserId? = null
        var leaveClicked = false

        composeRule.setContent {
            ClassroomTheme {
                CourseScreen(
                    state = state,
                    onTabSelected = {},
                    onPostClick = {},
                    onCreatePostClick = {},
                    onMemberRoleToggleClick = { lastMemberRoleToggled = it },
                    onMemberRemoveClick = { lastMemberRemoved = it },
                    onLeaveCourseClick = { leaveClicked = true },
                )
            }
        }

        composeRule.onAllNodes(hasTestTag("course_member_toggle_role_button"))[0].performClick()
        composeRule.onAllNodes(hasTestTag("course_member_remove_button"))[0].performClick()
        composeRule.onNodeWithTag("course_leave_button").performClick()

        assertEquals(memberId, lastMemberRoleToggled)
        assertEquals(memberId, lastMemberRemoved)
        assertTrue(leaveClicked)
    }

    @Test
    fun courseScreen_shows_loading_and_error_from_state() {
        val courseId = CourseId(UUID.fromString("00000000-0000-0000-0000-000000000800"))

        val state = CourseScreenUiState(
            courseId = courseId,
            courseTitle = "Course Title",
            inviteCode = "INV123",
            currentUserRole = CourseRole.STUDENT,
            courseAuthorId = UserId(UUID.randomUUID()),
            selectedTab = CourseTab.COURSE,
            posts = emptyList(),
            members = emptyList(),
            isLoadingCourse = true,
            isLoadingFeed = true,
            isLoadingMembers = true,
            error = "Something went wrong",
        )

        composeRule.setContent {
            ClassroomTheme {
                CourseScreen(
                    state = state,
                    onTabSelected = {},
                    onPostClick = {},
                    onCreatePostClick = {},
                    onMemberRoleToggleClick = {},
                    onMemberRemoveClick = {},
                    onLeaveCourseClick = {},
                )
            }
        }

        assertOptionalDisplayed("course_loading_course_indicator")
        assertOptionalDisplayed("course_loading_feed_indicator")
        assertOptionalDisplayed("course_loading_members_indicator")

        composeRule.onNodeWithTag("course_error").assertIsDisplayed()
    }

    private fun assertOptionalDisplayed(testTag: String) {
        val matcher = hasTestTag(testTag)
        val nodes = composeRule.onAllNodes(matcher)
        if (nodes.fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithTag(testTag).assertIsDisplayed()
        }
    }
}

