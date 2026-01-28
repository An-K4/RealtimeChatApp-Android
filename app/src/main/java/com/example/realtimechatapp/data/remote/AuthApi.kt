package com.example.realtimechatapp.data.remote

import com.example.realtimechatapp.data.remote.dto.LoginRequestDto
import com.example.realtimechatapp.data.remote.dto.LoginResponseDto
import com.example.realtimechatapp.data.remote.dto.LogoutResponseDto
import com.example.realtimechatapp.data.remote.dto.SignupRequestDto
import com.example.realtimechatapp.data.remote.dto.SignupResponseDto
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @POST("/auth/signup")
    suspend fun signup(@Body request: SignupRequestDto): SignupResponseDto

    @POST("/auth/logout")
    suspend fun logout(@Header("Authorization") token: String): LogoutResponseDto
}