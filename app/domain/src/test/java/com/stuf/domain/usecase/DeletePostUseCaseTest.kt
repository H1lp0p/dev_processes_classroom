package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostId
import com.stuf.domain.repository.PostRepository
import com.stuf.domain.usecase.impl.DeletePostUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

private class FakeDeletePostRepository : PostRepository {
    var lastDeletePostId: PostId? = null

    override suspend fun getCourseFeed(
        courseId: CourseId,
        skip: Int,
        take: Int,
    ): DomainResult<List<Post>> = error("Not needed in this fake")

    override suspend fun getPost(postId: PostId): DomainResult<Post> {
        error("Not needed in this fake")
    }

    override suspend fun createPost(courseId: CourseId, post: Post): DomainResult<Post> {
        error("Not needed in this fake")
    }

    override suspend fun updatePost(postId: PostId, post: Post): DomainResult<Post> {
        error("Not needed in this fake")
    }

    override suspend fun deletePost(postId: PostId): DomainResult<Unit> {
        lastDeletePostId = postId
        return DomainResult.Success(Unit)
    }
}

class DeletePostUseCaseTest {

    @Test
    fun `delegates to repository and returns success`() {
        val repo = FakeDeletePostRepository()
        val useCase : DeletePost = DeletePostUseCase(repo)
        val postId = PostId(UUID.randomUUID())

        val result = kotlinx.coroutines.runBlocking {
            useCase(postId)
        }

        assertTrue(result is DomainResult.Success<*>)
        assertEquals(postId, repo.lastDeletePostId)
    }
}
