package com.stuf.classroom.post

import androidx.lifecycle.SavedStateHandle
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Comment
import com.stuf.domain.model.CommentAuthor
import com.stuf.domain.model.CommentId
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostId
import com.stuf.domain.model.PostKind
import com.stuf.domain.model.Solution
import com.stuf.domain.model.SolutionId
import com.stuf.domain.model.TaskDetails
import com.stuf.domain.model.TaskId
import com.stuf.domain.model.UserId
import com.stuf.domain.usecase.AddCommentReply
import com.stuf.domain.usecase.AddPostComment
import com.stuf.domain.usecase.GetCommentReplies
import com.stuf.domain.usecase.GetPost
import com.stuf.domain.usecase.GetPostComments
import com.stuf.domain.usecase.GetSolutionsForTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.OffsetDateTime
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class PostScreenViewModelTest {

    private class FakeGetPost : GetPost {
        var result: DomainResult<Post> = DomainResult.Failure(DomainError.Unknown())
        var lastPostId: PostId? = null

        override suspend fun invoke(postId: PostId): DomainResult<Post> {
            lastPostId = postId
            return result
        }
    }

    private class FakeGetPostComments : GetPostComments {
        var result: DomainResult<List<Comment>> = DomainResult.Success(emptyList())
        var lastPostId: PostId? = null

        override suspend fun invoke(postId: PostId): DomainResult<List<Comment>> {
            lastPostId = postId
            return result
        }
    }

    private class FakeGetCommentReplies : GetCommentReplies {
        var result: DomainResult<List<Comment>> = DomainResult.Success(emptyList())
        var lastCommentId: CommentId? = null

        override suspend fun invoke(commentId: CommentId): DomainResult<List<Comment>> {
            lastCommentId = commentId
            return result
        }
    }

    private class FakeAddPostComment : AddPostComment {
        var result: DomainResult<Comment> = DomainResult.Failure(DomainError.Unknown())
        var lastPostId: PostId? = null
        var lastText: String? = null

        override suspend fun invoke(postId: PostId, text: String): DomainResult<Comment> {
            lastPostId = postId
            lastText = text
            return result
        }
    }

    private class FakeAddCommentReply : AddCommentReply {
        var result: DomainResult<Comment> = DomainResult.Failure(DomainError.Unknown())
        var lastCommentId: CommentId? = null
        var lastText: String? = null

        override suspend fun invoke(commentId: CommentId, text: String): DomainResult<Comment> {
            lastCommentId = commentId
            lastText = text
            return result
        }
    }

    private class FakeGetSolutionsForTask : GetSolutionsForTask {
        var result: DomainResult<List<Solution>> = DomainResult.Success(emptyList())
        var lastTaskId: TaskId? = null

        override suspend fun invoke(taskId: TaskId): DomainResult<List<Solution>> {
            lastTaskId = taskId
            return result
        }
    }

    private lateinit var fakeGetPost: FakeGetPost
    private lateinit var fakeGetPostComments: FakeGetPostComments
    private lateinit var fakeGetCommentReplies: FakeGetCommentReplies
    private lateinit var fakeAddPostComment: FakeAddPostComment
    private lateinit var fakeAddCommentReply: FakeAddCommentReply
    private lateinit var fakeGetSolutionsForTask: FakeGetSolutionsForTask

    private val testDispatcher: StandardTestDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: PostScreenViewModel

    private val postId: PostId = PostId(UUID.fromString("00000000-0000-0000-0000-000000000900"))

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeGetPost = FakeGetPost()
        fakeGetPostComments = FakeGetPostComments()
        fakeGetCommentReplies = FakeGetCommentReplies()
        fakeAddPostComment = FakeAddPostComment()
        fakeAddCommentReply = FakeAddCommentReply()
        fakeGetSolutionsForTask = FakeGetSolutionsForTask()

        viewModel = PostScreenViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf("postId" to postId.value.toString()),
            ),
            getPost = fakeGetPost,
            getPostComments = fakeGetPostComments,
            getCommentReplies = fakeGetCommentReplies,
            addPostComment = fakeAddPostComment,
            addCommentReply = fakeAddCommentReply,
            getSolutionsForTask = fakeGetSolutionsForTask,
            dispatcher = testDispatcher,
            currentUserRole = CourseRole.STUDENT,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load success populates post and comments for student`() = runTest(testDispatcher) {
        val courseId: com.stuf.domain.model.CourseId =
            com.stuf.domain.model.CourseId(UUID.fromString("00000000-0000-0000-0000-000000000901"))
        val taskDetails: TaskDetails? = null
        val post: Post = Post(
            id = postId,
            courseId = courseId,
            kind = PostKind.ANNOUNCEMENT,
            title = "Post title",
            text = "Post body",
            createdAt = OffsetDateTime.now(),
            taskDetails = taskDetails,
        )
        fakeGetPost.result = DomainResult.Success(post)

        val authorId: UserId = UserId(UUID.randomUUID())
        val comment: Comment = Comment(
            id = "c1",
            author = CommentAuthor(
                id = authorId,
                credentials = "User",
            ),
            text = "Comment",
            createdAt = OffsetDateTime.now(),
            isPrivate = false,
        )
        fakeGetPostComments.result = DomainResult.Success(listOf(comment))

        viewModel.onRetry()
        advanceUntilIdle()

        val state: PostUiState = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Post title", state.postTitle)
        assertEquals("Post body", state.postText)
        assertEquals(false, state.isTask)
        assertEquals(1, state.comments.size)
        assertEquals("Comment", state.comments[0].text)
    }

    @Test
    fun `initial load failure sets error`() = runTest(testDispatcher) {
        fakeGetPost.result = DomainResult.Failure(DomainError.Network())

        viewModel.onRetry()
        advanceUntilIdle()

        val state: PostUiState = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
    }

    @Test
    fun `onCommentSubmit_adds_public_comment_on_success`() = runTest(testDispatcher) {
        val courseId: com.stuf.domain.model.CourseId =
            com.stuf.domain.model.CourseId(UUID.randomUUID())
        val post: Post = Post(
            id = postId,
            courseId = courseId,
            kind = PostKind.ANNOUNCEMENT,
            title = "Post",
            text = "Body",
            createdAt = OffsetDateTime.now(),
            taskDetails = null,
        )
        fakeGetPost.result = DomainResult.Success(post)
        fakeGetPostComments.result = DomainResult.Success(emptyList())

        val createdComment: Comment = Comment(
            id = "new",
            author = CommentAuthor(
                id = UserId(UUID.randomUUID()),
                credentials = "User",
            ),
            text = "Hello",
            createdAt = OffsetDateTime.now(),
            isPrivate = false,
        )
        fakeAddPostComment.result = DomainResult.Success(createdComment)

        viewModel.onRetry()
        advanceUntilIdle()

        viewModel.onCommentSubmit(text = "Hello", isPrivate = false, parentCommentId = null)
        advanceUntilIdle()

        val state: PostUiState = viewModel.uiState.value
        assertEquals("Hello", fakeAddPostComment.lastText)
        assertEquals(1, state.comments.size)
        assertEquals("Hello", state.comments[0].text)
    }

    @Test
    fun `onLoadRepliesClick_loads_and_merges_replies`() = runTest(testDispatcher) {
        val courseId: com.stuf.domain.model.CourseId =
            com.stuf.domain.model.CourseId(UUID.randomUUID())
        val post: Post = Post(
            id = postId,
            courseId = courseId,
            kind = PostKind.ANNOUNCEMENT,
            title = "Post",
            text = "Body",
            createdAt = OffsetDateTime.now(),
            taskDetails = null,
        )
        fakeGetPost.result = DomainResult.Success(post)

        val parent: Comment = Comment(
            id = "c1",
            author = CommentAuthor(
                id = UserId(UUID.randomUUID()),
                credentials = "User",
            ),
            text = "Parent",
            createdAt = OffsetDateTime.now(),
            isPrivate = false,
        )
        fakeGetPostComments.result = DomainResult.Success(listOf(parent))

        val reply: Comment = Comment(
            id = "c2",
            author = CommentAuthor(
                id = UserId(UUID.randomUUID()),
                credentials = "User",
            ),
            text = "Reply",
            createdAt = OffsetDateTime.now(),
            isPrivate = false,
        )
        fakeGetCommentReplies.result = DomainResult.Success(listOf(reply))

        viewModel.onRetry()
        advanceUntilIdle()

        viewModel.onLoadRepliesClick(CommentId("c1"))
        advanceUntilIdle()

        val state: PostUiState = viewModel.uiState.value
        assertEquals(CommentId("c1"), fakeGetCommentReplies.lastCommentId)
        assertEquals(1, state.comments.size)
        val first: CommentUi = state.comments[0]
        assertEquals(1, first.replies.size)
        assertEquals("Reply", first.replies[0].text)
    }

    @Test
    fun `initial load as_teacher_for_task_populates_solutions`() = runTest(testDispatcher) {
        val courseId: com.stuf.domain.model.CourseId =
            com.stuf.domain.model.CourseId(UUID.randomUUID())
        val taskDetails: TaskDetails = TaskDetails(
            deadline = OffsetDateTime.now(),
            isMandatory = true,
            maxScore = 10,
        )
        val taskPost: Post = Post(
            id = postId,
            courseId = courseId,
            kind = PostKind.TASK,
            title = "Task",
            text = "Solve",
            createdAt = OffsetDateTime.now(),
            taskDetails = taskDetails,
        )
        fakeGetPost.result = DomainResult.Success(taskPost)
        fakeGetPostComments.result = DomainResult.Success(emptyList())

        val taskId: TaskId = TaskId(UUID.randomUUID())
        val solution: Solution = Solution(
            id = SolutionId(UUID.randomUUID()),
            taskId = taskId,
            author = com.stuf.domain.model.User(
                id = UserId(UUID.randomUUID()),
                credentials = "Student",
                email = "student@example.com",
            ),
            text = "Answer",
            createdAt = OffsetDateTime.now(),
            score = null,
            status = com.stuf.domain.model.SolutionStatus.PENDING,
        )
        fakeGetSolutionsForTask.result = DomainResult.Success(listOf(solution))

        // пересоздадим VM как учителя
        viewModel = PostScreenViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf("postId" to postId.value.toString()),
            ),
            getPost = fakeGetPost,
            getPostComments = fakeGetPostComments,
            getCommentReplies = fakeGetCommentReplies,
            addPostComment = fakeAddPostComment,
            addCommentReply = fakeAddCommentReply,
            getSolutionsForTask = fakeGetSolutionsForTask,
            dispatcher = testDispatcher,
            currentUserRole = CourseRole.TEACHER,
        )

        viewModel.onRetry()
        advanceUntilIdle()

        val state: PostUiState = viewModel.uiState.value
        assertTrue(state.isTask)
        assertEquals(1, state.solutions.size)
        assertEquals("Student", state.solutions[0].studentName)
    }
}

