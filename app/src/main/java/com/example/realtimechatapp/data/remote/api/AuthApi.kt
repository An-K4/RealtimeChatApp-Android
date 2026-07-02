package com.example.realtimechatapp.data.remote.api

import com.example.realtimechatapp.data.remote.dto.user.UserResponseDto
import com.example.realtimechatapp.data.remote.dto.auth.LoginRequestDto
import com.example.realtimechatapp.data.remote.dto.auth.LoginResponseDto
import com.example.realtimechatapp.data.remote.dto.auth.SignupRequestDto
import com.example.realtimechatapp.data.remote.dto.MessageResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @POST("/auth/signup")
    suspend fun signup(@Body request: SignupRequestDto): MessageResponseDto

    @POST("/auth/logout")
    suspend fun logout(): MessageResponseDto

    @GET("/auth/me")
    suspend fun getMe(): UserResponseDto
}