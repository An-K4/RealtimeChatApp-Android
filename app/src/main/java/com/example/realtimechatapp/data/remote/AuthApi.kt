package com.example.realtimechatapp.data.remote

import com.example.realtimechatapp.data.remote.dto.UserResponseDto
import com.example.realtimechatapp.data.remote.dto.LoginRequestDto
import com.example.realtimechatapp.data.remote.dto.LoginResponseDto
import com.example.realtimechatapp.data.remote.dto.SignupRequestDto
import com.example.realtimechatapp.data.remote.dto.SimpleResponseDto
import com.example.realtimechatapp.data.remote.dto.UploadResponseDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AuthApi {
    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto
    @Multipart
    @POST("/auth/upload-avatar")
    suspend fun uploadAvatar(@Part file: MultipartBody.Part): UploadResponseDto
    @POST("/auth/signup")
    suspend fun signup(@Body request: SignupRequestDto): SimpleResponseDto
    @POST("/auth/logout")
    suspend fun logout(): SimpleResponseDto
    @GET("/auth/me")
    suspend fun getMe(): UserResponseDto
}