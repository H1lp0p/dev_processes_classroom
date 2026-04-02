package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostId

interface UpdatePost {
    suspend operator fun invoke(postId: PostId, post: Post): DomainResult<Post>
}
