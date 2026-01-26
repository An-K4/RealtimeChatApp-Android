package com.example.realtimechatapp.data.remote

import com.example.realtimechatapp.data.remote.dto.LoginRequestDto
import com.example.realtimechatapp.data.remote.dto.LoginResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto
}