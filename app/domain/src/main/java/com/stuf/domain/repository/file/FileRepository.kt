package com.stuf.domain.repository

import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.FileInfo

interface FileRepository {
    suspend fun uploadFile(bytes: ByteArray, name: String): DomainResult<FileInfo>
}
