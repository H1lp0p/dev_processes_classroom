package com.stuf.classroom.post

data class CommentUi(
    val id: String,
    val authorName: String,
    val text: String,
    val isPrivate: Boolean = false,
    val replies: List<CommentUi> = emptyList(),
)
