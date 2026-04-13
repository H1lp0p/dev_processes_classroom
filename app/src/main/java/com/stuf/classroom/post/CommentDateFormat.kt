package com.stuf.classroom.post

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val commentDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale("ru"))

internal fun formatCommentDate(createdAt: OffsetDateTime): String = createdAt.format(commentDateFormatter)
