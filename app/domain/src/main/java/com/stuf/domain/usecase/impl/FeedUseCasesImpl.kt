package com.stuf.domain.usecase.impl

import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.CourseId
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostId
import com.stuf.domain.repository.PostRepository
import com.stuf.domain.usecase.CreatePost
import com.stuf.domain.usecase.DeletePost
import com.stuf.domain.usecase.GetCourseFeed
import com.stuf.domain.usecase.GetPost
import com.stuf.domain.usecase.UpdatePost

class GetCourseFeedUseCase(
    private val repository: PostRepository,
) : GetCourseFeed {

    override suspend fun invoke(
        courseId: CourseId,
        skip: Int,
        take: Int,
    ): DomainResult<List<Post>> {
        if (skip < 0) {
            return DomainResult.Failure(DomainError.Validation("skip must be >= 0"))
        }
        if (take <= 0) {
            return DomainResult.Failure(DomainError.Validation("take must be > 0"))
        }
        return repository.getCourseFeed(courseId, skip, take)
    }
}

class GetPostUseCase(
    private val repository: PostRepository,
) : GetPost {

    override suspend fun invoke(postId: PostId): DomainResult<Post> {
        return repository.getPost(postId)
    }
}

class CreatePostUseCase(
    private val repository: PostRepository,
) : CreatePost {

    override suspend fun invoke(courseId: CourseId, post: Post): DomainResult<Post> {
        return repository.createPost(courseId, post)
    }
}

class UpdatePostUseCase(
    private val repository: PostRepository,
) : UpdatePost {

    override suspend fun invoke(postId: PostId, post: Post): DomainResult<Post> {
        return repository.updatePost(postId, post)
    }
}

class DeletePostUseCase(
    private val repository: PostRepository,
) : DeletePost {

    override suspend fun invoke(postId: PostId): DomainResult<Unit> {
        return repository.deletePost(postId)
    }
}

