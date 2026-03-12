package com.stuf.domain.usecase

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostId
import com.stuf.domain.repository.PostRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.OffsetDateTime
import java.util.UUID

private class FakeGetPostRepository : PostRepository {
    var lastGetPostId: PostId? = null
    val missingPostId: PostId = PostId(UUID.randomUUID())
    private val samplePost = Post(
        id = PostId(UUID.randomUUID()),
        courseId = com.stuf.domain.model.CourseId(UUID.randomUUID()),
        kind = com.stuf.domain.model.PostKind.ANNOUNCEMENT,
        title = "Test",
        text = "Body",
        createdAt = OffsetDateTime.now(),
        taskDetails = null,
    )

    override suspend fun getCourseFeed(
        courseId: com.stuf.domain.model.CourseId,
        skip: Int,
        take: Int,
    ): DomainResult<List<Post>> = error("Not needed in this fake")

    override suspend fun getPost(postId: PostId): DomainResult<Post> {
        if (postId == missingPostId) {
            return DomainResult.Failure(DomainError.NotFound)
        }
        lastGetPostId = postId
        return DomainResult.Success(samplePost.copy(id = postId))
    }

    override suspend fun createPost(
        courseId: com.stuf.domain.model.CourseId,
        post: Post,
    ): DomainResult<Post> {
        error("Not needed in this fake")
    }

    override suspend fun updatePost(postId: PostId, post: Post): DomainResult<Post> {
        error("Not needed in this fake")
    }

    override suspend fun deletePost(postId: PostId): DomainResult<Unit> {
        error("Not needed in this fake")
    }
}

class GetPostUseCaseTest {

    @Test
    fun `delegates to repository and returns post`() {
        val repo = FakeGetPostRepository()
        val useCase : GetPost = GetPostUseCase(repo)
        val postId = PostId(UUID.randomUUID())

        val result = kotlinx.coroutines.runBlocking {
            useCase(postId)
        }

        assertTrue(result is DomainResult.Success<*>)
        assertEquals(postId, repo.lastGetPostId)
        assertEquals(postId, (result as DomainResult.Success<Post>).value.id)
    }

    @Test
    fun `returns not found error when repository returns NotFound`() {
        val repo = FakeGetPostRepository()
        val useCase : GetPost = GetPostUseCase(repo)
        val missingId = repo.missingPostId

        val result = kotlinx.coroutines.runBlocking {
            useCase(missingId)
        }

        assertTrue(result is DomainResult.Failure)
        val error = (result as DomainResult.Failure).error
        assertEquals(DomainError.NotFound, error)
    }
}
