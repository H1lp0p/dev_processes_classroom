package com.stuf.data.repository

import com.stuf.data.model.CreateUpdatePostDto
import com.stuf.data.model.PostType
import com.stuf.data.model.TaskType
import com.stuf.domain.model.AnnouncementPost
import com.stuf.domain.model.MaterialPost
import com.stuf.domain.model.Post
import com.stuf.domain.model.TaskPost
import com.stuf.domain.model.TeamTaskPost

internal fun Post.toCreateUpdateDto(): CreateUpdatePostDto {
    val taskType = TaskType.mandatory
    return when (this) {
        is AnnouncementPost ->
            CreateUpdatePostDto(
                type = PostType.post,
                title = title,
                text = text,
                deadline = null,
                maxScore = null,
                taskType = null,
                solvableAfterDeadline = null,
                files = null,
            )
        is MaterialPost ->
            CreateUpdatePostDto(
                type = PostType.post,
                title = title,
                text = text,
                deadline = null,
                maxScore = null,
                taskType = null,
                solvableAfterDeadline = null,
                files = files.mapNotNull { it.id },
            )
        is TaskPost ->
            CreateUpdatePostDto(
                type = PostType.task,
                title = title,
                text = text,
                deadline = taskDetails.deadline,
                maxScore = taskDetails.maxScore,
                taskType = taskType,
                solvableAfterDeadline = taskDetails.isMandatory,
                files = attachments.mapNotNull { it.id },
            )
        is TeamTaskPost ->
            CreateUpdatePostDto(
                type = ApiPostTypeTeamTask,
                title = title,
                text = text,
                deadline = taskDetails.deadline,
                maxScore = taskDetails.maxScore,
                taskType = taskType,
                solvableAfterDeadline = taskDetails.isMandatory,
                files = attachments.mapNotNull { it.id },
            )
    }
}
