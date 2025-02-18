package com.example.careplus.data.api


import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface FileUploadApi {
    @Multipart
    @POST("upload/file")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Query("folder") folder: String
    ): Response<FileUploadResponse>
}

data class FileUploadResponse(
    val error: Boolean,
    val data: FileUploadData
)

data class FileUploadData(
    val file_path: String,
    val url: String,
    val file_name: String,
    val folder: String
)