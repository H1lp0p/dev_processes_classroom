package com.stuf.domain.usecase

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.Post
import com.stuf.domain.repository.PostRepository
import com.stuf.domain.usecase.impl.GetCourseFeedUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

private class FakePostRepositoryForFeed : PostRepository {
    var lastFeedArgs: Triple<CourseId, Int, Int>? = null
    private val emptyFeed = emptyList<Post>()

    override suspend fun getCourseFeed(
        courseId: CourseId,
        skip: Int,
        take: Int,
    ): DomainResult<List<Post>> {
        lastFeedArgs = Triple(courseId, skip, take)
        return DomainResult.Success(emptyFeed)
    }

    override suspend fun getPost(postId: com.stuf.domain.model.PostId): DomainResult<Post> {
        error("Not needed in this fake")
    }

    override suspend fun createPost(
        courseId: CourseId,
        post: Post,
    ): DomainResult<Post> {
        error("Not needed in this fake")
    }

    override suspend fun updatePost(
        postId: com.stuf.domain.model.PostId,
        post: Post,
    ): DomainResult<Post> {
        error("Not needed in this fake")
    }

    override suspend fun deletePost(postId: com.stuf.domain.model.PostId): DomainResult<Unit> {
        error("Not needed in this fake")
    }
}

class GetCourseFeedUseCaseTest {

    @Test
    fun `valid skip and take delegates to repository and returns success`() {
        val repo = FakePostRepositoryForFeed()
        val useCase : GetCourseFeed = GetCourseFeedUseCase(repo)
        val courseId = CourseId(UUID.randomUUID())

        val result = kotlinx.coroutines.runBlocking {
            useCase(courseId, skip = 0, take = 10)
        }

        assertTrue(result is DomainResult.Success<*>)
        assertEquals(courseId, repo.lastFeedArgs?.first)
        assertEquals(0, repo.lastFeedArgs?.second)
        assertEquals(10, repo.lastFeedArgs?.third)
    }

    @Test
    fun `skip less than zero returns validation error and does not call repository`() {
        val repo = FakePostRepositoryForFeed()
        val useCase : GetCourseFeed = GetCourseFeedUseCase(repo)
        val courseId = CourseId(UUID.randomUUID())

        val result = kotlinx.coroutines.runBlocking {
            useCase(courseId, skip = -1, take = 10)
        }

        assertTrue(result is DomainResult.Failure)
        assertTrue((result as DomainResult.Failure).error is DomainError.Validation)
        assertEquals(null, repo.lastFeedArgs)
    }

    @Test
    fun `take zero or less returns validation error and does not call repository`() {
        val repo = FakePostRepositoryForFeed()
        val useCase : GetCourseFeed = GetCourseFeedUseCase(repo)
        val courseId = CourseId(UUID.randomUUID())

        val result = kotlinx.coroutines.runBlocking {
            useCase(courseId, skip = 0, take = 0)
        }

        assertTrue(result is DomainResult.Failure)
        assertTrue((result as DomainResult.Failure).error is DomainError.Validation)
        assertEquals(null, repo.lastFeedArgs)
    }
}
