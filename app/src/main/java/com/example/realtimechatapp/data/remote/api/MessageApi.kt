package com.example.realtimechatapp.data.remote.api

import com.example.realtimechatapp.data.remote.dto.GetMessageResponseDto
import com.example.realtimechatapp.data.remote.dto.GetUserResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

interface MessageApi {
    @GET("/messages/users")
    suspend fun getUsers(): GetUserResponseDto

    @GET("/messages/{id}")
    suspend fun getMessage(@Path("id") friendId: String): GetMessageResponseDto
}