package com.stuf.domain.model

import java.util.UUID

@JvmInline
value class UserId(val value: UUID)

@JvmInline
value class CourseId(val value: UUID)

@JvmInline
value class PostId(val value: UUID)

@JvmInline
value class TaskId(val value: UUID)

@JvmInline
value class SolutionId(val value: UUID)

@JvmInline
value class CommentId(val value: String)

