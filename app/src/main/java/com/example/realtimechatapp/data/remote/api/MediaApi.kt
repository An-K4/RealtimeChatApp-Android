package com.example.realtimechatapp.data.remote.api

import com.example.realtimechatapp.data.remote.dto.UploadResponseDto
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface MediaApi {
    @Multipart
    @POST("/media/public-upload")
    suspend fun publicUpload(@Part file: MultipartBody.Part): UploadResponseDto

    @Multipart
    @POST("/media/upload")
    suspend fun upload(@Part file: MultipartBody.Part): UploadResponseDto
}