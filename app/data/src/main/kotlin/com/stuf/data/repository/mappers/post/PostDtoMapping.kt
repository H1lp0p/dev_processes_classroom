package com.stuf.data.repository

import com.stuf.data.model.CreateUpdatePostDto
import com.stuf.data.model.PostType
import com.stuf.data.model.TaskType
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostKind

internal fun Post.toCreateUpdateDto(): CreateUpdatePostDto {
    val taskType = if (kind == PostKind.TASK) TaskType.mandatory else TaskType.mandatory
    return CreateUpdatePostDto(
        type = when (kind) {
            PostKind.ANNOUNCEMENT,
            PostKind.MATERIAL,
            -> PostType.post
            PostKind.TASK -> PostType.task
        },
        title = title,
        text = text,
        deadline = taskDetails?.deadline,
        maxScore = taskDetails?.maxScore,
        taskType = taskType,
        solvableAfterDeadline = taskDetails?.isMandatory,
        files = null,
    )
}
