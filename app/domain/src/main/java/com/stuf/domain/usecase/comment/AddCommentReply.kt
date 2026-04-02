package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.Comment
import com.stuf.domain.model.CommentId

interface AddCommentReply {
    suspend operator fun invoke(commentId: CommentId, text: String): DomainResult<Comment>
}
