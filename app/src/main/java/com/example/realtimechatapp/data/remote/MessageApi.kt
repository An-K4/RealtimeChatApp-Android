package com.example.realtimechatapp.data.remote

import com.example.realtimechatapp.data.remote.dto.GetUserResponseDto
import retrofit2.http.GET

interface MessageApi {
    @GET("/messages/users")
    suspend fun getUsers(): GetUserResponseDto
}