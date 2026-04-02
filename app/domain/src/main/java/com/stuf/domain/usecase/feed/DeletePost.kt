package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.PostId

interface DeletePost {
    suspend operator fun invoke(postId: PostId): DomainResult<Unit>
}
