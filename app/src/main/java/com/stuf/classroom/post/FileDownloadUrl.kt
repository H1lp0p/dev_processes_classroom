package com.stuf.classroom.post

import java.util.UUID

fun buildFileDownloadUrl(apiBaseUrl: String, fileId: UUID): String {
    val base = apiBaseUrl.trimEnd('/')
    return "$base/api/files/$fileId"
}
