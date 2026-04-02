package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Comment
import com.stuf.domain.model.PostId

interface AddPostComment {
    suspend operator fun invoke(postId: PostId, text: String): DomainResult<Comment>
}
