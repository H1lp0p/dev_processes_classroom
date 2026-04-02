package com.stuf.classroom.post

import com.stuf.domain.model.PostKind

fun PostKind.toPostScreenTypeLabel(): String =
    when (this) {
        PostKind.ANNOUNCEMENT -> "Пост"
        PostKind.MATERIAL -> "Материал"
        PostKind.TASK -> "Задание"
        PostKind.TEAM_TASK -> "Командное задание"
    }
