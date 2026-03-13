package com.stuf.domain.model

import java.time.OffsetDateTime

data class Comment(
    val id: String,
    val author: CommentAuthor,
    val text: String,
    val createdAt: OffsetDateTime,
    val isPrivate: Boolean,
)

data class CommentAuthor(
    val id: UserId,
    val credentials: String,
)

