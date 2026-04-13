package com.stuf.classroom.post

import androidx.lifecycle.SavedStateHandle
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.AnnouncementPost
import com.stuf.domain.model.Comment
import com.stuf.domain.model.CommentAuthor
import com.stuf.domain.model.CommentId
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.MaterialPost
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostAttachment
import com.stuf.domain.model.PostId
import com.stuf.domain.model.TaskDetails
import com.stuf.domain.model.TaskPost
import com.stuf.domain.model.TeamTaskPost
import com.stuf.domain.model.UserId
import com.stuf.domain.usecase.AddCommentReply
import com.stuf.domain.usecase.AddPostComment
import com.stuf.domain.usecase.GetCommentReplies
import com.stuf.domain.usecase.GetPost
import com.stuf.domain.usecase.GetPostComments
import java.time.OffsetDateTime
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

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

    private lateinit var fakeGetPost: FakeGetPost
    private lateinit var fakeGetPostComments: FakeGetPostComments
    private lateinit var fakeGetCommentReplies: FakeGetCommentReplies
    private lateinit var fakeAddPostComment: FakeAddPostComment
    private lateinit var fakeAddCommentReply: FakeAddCommentReply

    private lateinit var viewModel: PostScreenViewModel

    private val postId: PostId = PostId(UUID.fromString("00000000-0000-0000-0000-000000000900"))

    @Before
    fun setUp() {
        fakeGetPost = FakeGetPost()
        fakeGetPostComments = FakeGetPostComments()
        fakeGetCommentReplies = FakeGetCommentReplies()
        fakeAddPostComment = FakeAddPostComment()
        fakeAddCommentReply = FakeAddCommentReply()

        viewModel =
            PostScreenViewModel(
                savedStateHandle =
                    SavedStateHandle(
                        mapOf(
                            "postId" to postId.value.toString(),
                            "role" to "student",
                        ),
                    ),
                getPost = fakeGetPost,
                getPostComments = fakeGetPostComments,
                getCommentReplies = fakeGetCommentReplies,
                addPostComment = fakeAddPostComment,
                addCommentReply = fakeAddCommentReply,
                dispatcher = Dispatchers.Unconfined,
            )
    }

    @Test
    fun `initial load success populates post and comments for student`() = runTest {
        val courseId = CourseId(UUID.fromString("00000000-0000-0000-0000-000000000901"))
        val post: Post =
            AnnouncementPost(
                id = postId,
                courseId = courseId,
                title = "Post title",
                text = "Post body",
                createdAt = OffsetDateTime.now(),
            )
        fakeGetPost.result = DomainResult.Success(post)

        val authorId: UserId = UserId(UUID.randomUUID())
        val comment: Comment =
            Comment(
                id = "c1",
                author =
                    CommentAuthor(
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
        assertFalse(state.isLoadingPost)
        val content = state.content as PostScreenContent.Announcement
        assertEquals("Post title", content.post.title)
        assertEquals("Post body", content.post.text)
        assertEquals(1, state.comments.size)
        assertEquals("Comment", state.comments[0].text)
    }

    @Test
    fun `initial load sets material post content`() = runTest {
        val courseId = CourseId(UUID.randomUUID())
        val post: Post =
            MaterialPost(
                id = postId,
                courseId = courseId,
                title = "PDF",
                text = "Текст",
                createdAt = OffsetDateTime.now(),
                files = listOf(PostAttachment(id = UUID.randomUUID(), name = "a.pdf")),
            )
        fakeGetPost.result = DomainResult.Success(post)
        fakeGetPostComments.result = DomainResult.Success(emptyList())

        viewModel.onRetry()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.content is PostScreenContent.Material)
    }

    @Test
    fun `initial load sets team task content`() = runTest {
        val courseId = CourseId(UUID.randomUUID())
        val details = TaskDetails(OffsetDateTime.now(), true, 10)
        val post: Post =
            TeamTaskPost(
                id = postId,
                courseId = courseId,
                title = "Команда",
                text = "Описание",
                createdAt = OffsetDateTime.now(),
                taskDetails = details,
            )
        fakeGetPost.result = DomainResult.Success(post)
        fakeGetPostComments.result = DomainResult.Success(emptyList())

        viewModel.onRetry()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.content is PostScreenContent.TeamTask)
    }

    @Test
    fun `initial load failure sets error`() = runTest {
        fakeGetPost.result = DomainResult.Failure(DomainError.Network())

        viewModel.onRetry()
        advanceUntilIdle()

        val state: PostUiState = viewModel.uiState.value
        assertFalse(state.isLoadingPost)
        assertNotNull(state.postLoadError)
    }

    @Test
    fun `onCommentSubmit_adds_public_comment_on_success`() = runTest {
        val courseId = CourseId(UUID.randomUUID())
        val post: Post =
            AnnouncementPost(
                id = postId,
                courseId = courseId,
                title = "Post",
                text = "Body",
                createdAt = OffsetDateTime.now(),
            )
        fakeGetPost.result = DomainResult.Success(post)
        fakeGetPostComments.result = DomainResult.Success(emptyList())

        val createdComment: Comment =
            Comment(
                id = "new",
                author =
                    CommentAuthor(
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
    fun `onLoadRepliesClick_loads_and_merges_replies`() = runTest {
        val courseId = CourseId(UUID.randomUUID())
        val post: Post =
            AnnouncementPost(
                id = postId,
                courseId = courseId,
                title = "Post",
                text = "Body",
                createdAt = OffsetDateTime.now(),
            )
        fakeGetPost.result = DomainResult.Success(post)

        val parent: Comment =
            Comment(
                id = "c1",
                author =
                    CommentAuthor(
                        id = UserId(UUID.randomUUID()),
                        credentials = "User",
                    ),
                text = "Parent",
                createdAt = OffsetDateTime.now(),
                isPrivate = false,
            )
        fakeGetPostComments.result = DomainResult.Success(listOf(parent))

        val reply: Comment =
            Comment(
                id = "c2",
                author =
                    CommentAuthor(
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
    fun `initial load task post exposes task screen content`() = runTest {
        val courseId = CourseId(UUID.randomUUID())
        val taskDetails: TaskDetails =
            TaskDetails(
                deadline = OffsetDateTime.now(),
                isMandatory = true,
                maxScore = 10,
            )
        val taskPost: Post =
            TaskPost(
                id = postId,
                courseId = courseId,
                title = "Task",
                text = "Solve",
                createdAt = OffsetDateTime.now(),
                taskDetails = taskDetails,
            )
        fakeGetPost.result = DomainResult.Success(taskPost)
        fakeGetPostComments.result = DomainResult.Success(emptyList())

        viewModel.onRetry()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.content is PostScreenContent.Task)
    }
}
