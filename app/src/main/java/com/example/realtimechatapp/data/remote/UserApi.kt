package com.example.realtimechatapp.data.remote

import com.example.realtimechatapp.data.remote.dto.ChangePasswordRequestDto
import com.example.realtimechatapp.data.remote.dto.SimpleResponseDto
import com.example.realtimechatapp.data.remote.dto.UpdateProfileRequestDto
import com.example.realtimechatapp.data.remote.dto.UploadResponseDto
import com.example.realtimechatapp.data.remote.dto.UserDto
import com.example.realtimechatapp.data.remote.dto.UserResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part

interface UserApi {
    @PATCH("/users/update")
    suspend fun updateProfile(@Body requestBody: UpdateProfileRequestDto): UserResponseDto

    @PATCH("/users/upload-avatar")
    @Multipart
    suspend fun updateAvatar(@Part file: MultipartBody.Part): UploadResponseDto

    @POST("/users/change-password")
    suspend fun changePassword(@Body requestBody: ChangePasswordRequestDto): SimpleResponseDto
}