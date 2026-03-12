package com.stuf.data.repository

import com.stuf.data.api.FilesApi
import com.stuf.data.model.ApiResponseType
import com.stuf.data.model.IdRequestDto
import com.stuf.data.model.IdRequestDtoApiResponse
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.FileInfo
import com.stuf.domain.repository.FileRepository
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.util.UUID

private class FakeFilesApi : FilesApi {
    var lastUploadedPart: MultipartBody.Part? = null
    var uploadResponse: Response<IdRequestDtoApiResponse>? = null

    override suspend fun apiFilesIdGet(id: UUID): Response<Unit> {
        error("Not needed in these tests")
    }

    override suspend fun apiFilesUploadPost(file: MultipartBody.Part?): Response<IdRequestDtoApiResponse> {
        lastUploadedPart = file
        return requireNotNull(uploadResponse)
    }
}

class FileRepositoryImplTest {

    @Test
    fun `uploadFile success returns FileInfo with id and name`() {
        val api = FakeFilesApi().apply {
            uploadResponse = Response.success(
                IdRequestDtoApiResponse(
                    type = ApiResponseType.success,
                    message = null,
                    data = IdRequestDto(
                        id = UUID.fromString("00000000-0000-0000-0000-0000000000ff"),
                    ),
                ),
            )
        }
        val repository: FileRepository = FileRepositoryImpl(api)

        val bytes = "content".toByteArray()
        val result = runBlocking {
            repository.uploadFile(bytes = bytes, name = "file.txt")
        }

        assertTrue(result is DomainResult.Success<FileInfo>)
        val fileInfo = (result as DomainResult.Success<FileInfo>).value
        assertEquals("00000000-0000-0000-0000-0000000000ff", fileInfo.id)
        assertEquals("file.txt", fileInfo.name)

        // проверяем, что multipart сформирован
        val part = api.lastUploadedPart
        assertEquals("file", part?.headers?.get("Content-Disposition")?.substringBefore(";"))
    }

    @Test
    fun `uploadFile http 401 returns Unauthorized`() {
        val api = FakeFilesApi().apply {
            val body = ResponseBody.create("application/json".toMediaType(), "{}")
            uploadResponse = Response.error(401, body)
        }
        val repository: FileRepository = FileRepositoryImpl(api)

        val result = runBlocking {
            repository.uploadFile(bytes = ByteArray(0), name = "file.txt")
        }

        assertTrue(result is DomainResult.Failure)
        assertEquals(DomainError.Unauthorized, (result as DomainResult.Failure).error)
    }
}

