package com.stuf.classroom.post

data class CommentUi(
    val id: String,
    val authorName: String,
    val text: String,
    val createdAtLabel: String,
    val isPrivate: Boolean = false,
    val replies: List<CommentUi> = emptyList(),
    /** Ответы для этого комментария уже запрашивались с сервера. */
    val repliesLoaded: Boolean = false,
)
