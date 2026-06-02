package com.example.realtimechatapp.data.remote.api

import com.example.realtimechatapp.data.remote.dto.user.ChangePasswordRequestDto
import com.example.realtimechatapp.data.remote.dto.SimpleResponseDto
import com.example.realtimechatapp.data.remote.dto.user.SearchResponseDto
import com.example.realtimechatapp.data.remote.dto.user.UpdateProfileRequestDto
import com.example.realtimechatapp.data.remote.dto.user.UploadResponseDto
import com.example.realtimechatapp.data.remote.dto.user.UserResponseDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface UserApi {
    @PATCH("/users/update")
    suspend fun updateProfile(@Body requestBody: UpdateProfileRequestDto): UserResponseDto

    @PATCH("/users/upload-avatar")
    @Multipart
    suspend fun updateAvatar(@Part file: MultipartBody.Part): UploadResponseDto

    @POST("/users/change-password")
    suspend fun changePassword(@Body requestBody: ChangePasswordRequestDto): SimpleResponseDto

    @GET("/users/search")
    suspend fun performSearch(@Query("keyword") keyword: String): SearchResponseDto
}