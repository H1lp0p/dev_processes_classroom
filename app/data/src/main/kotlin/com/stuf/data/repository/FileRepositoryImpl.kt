package com.stuf.data.repository

import com.stuf.data.api.FilesApi
import com.stuf.data.common.httpCodeToDomainError
import com.stuf.data.model.ApiResponseType
import com.stuf.domain.common.DomainError
import com.stuf.domain.common.DomainResult
import com.stuf.domain.model.FileInfo
import com.stuf.domain.repository.FileRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class FileRepositoryImpl @Inject constructor(
    private val api: FilesApi,
) : FileRepository {

    override suspend fun uploadFile(bytes: ByteArray, name: String): DomainResult<FileInfo> {
        val mediaType = "application/octet-stream".toMediaTypeOrNull()
        val body = bytes.toRequestBody(mediaType)
        val part = MultipartBody.Part.createFormData("file", name, body)

        val response =
            try {
                api.apiFilesUploadPost(part)
            } catch (e: IOException) {
                return DomainResult.Failure(DomainError.Network(e))
            } catch (e: Exception) {
                return DomainResult.Failure(DomainError.Unknown(e))
            }

        if (!response.isSuccessful) {
            return DomainResult.Failure(httpCodeToDomainError(response.code()))
        }

        val envelope = response.body() ?: return DomainResult.Failure(DomainError.Unknown())
        if (envelope.type != ApiResponseType.success || envelope.data == null) {
            return DomainResult.Failure(DomainError.Validation(envelope.message ?: "Upload failed"))
        }

        val id = envelope.data.id
        return DomainResult.Success(
            FileInfo(
                id = id.toString(),
                name = name,
            ),
        )
    }
}
