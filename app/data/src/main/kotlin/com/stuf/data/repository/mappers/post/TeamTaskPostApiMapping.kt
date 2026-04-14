package com.stuf.data.repository

import com.stuf.data.model.PostType
import com.stuf.domain.model.TeamTaskPost

/**
 * Значение [PostType] из OpenAPI для командного задания (в спецификации — `teaM_TASK`).
 * В домене соответствует [TeamTaskPost] (командное задание / `team_task` с точки зрения продукта).
 */
internal val ApiPostTypeTeamTask: PostType = PostType.teaM_TASK
