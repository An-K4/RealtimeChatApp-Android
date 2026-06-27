package com.example.realtimechatapp.data.remote.api

import com.example.realtimechatapp.data.remote.dto.user.UserResponseDto
import com.example.realtimechatapp.data.remote.dto.auth.LoginRequestDto
import com.example.realtimechatapp.data.remote.dto.auth.LoginResponseDto
import com.example.realtimechatapp.data.remote.dto.auth.SignupRequestDto
import com.example.realtimechatapp.data.remote.dto.SimpleResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @POST("/auth/signup")
    suspend fun signup(@Body request: SignupRequestDto): SimpleResponseDto

    @POST("/auth/logout")
    suspend fun logout(): SimpleResponseDto

    @GET("/auth/me")
    suspend fun getMe(): UserResponseDto
}