package com.example.realtimechatapp.data.remote

import com.example.realtimechatapp.data.remote.dto.GetMessageResponseDto
import com.example.realtimechatapp.data.remote.dto.GetUserResponseDto
import com.example.realtimechatapp.data.remote.dto.MessageDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path

interface MessageApi {
    @GET("/messages/users")
    suspend fun getUsers(): GetUserResponseDto

    @GET("/messages/{id}")
    suspend fun getMessage(@Path("id") friendId: String): GetMessageResponseDto
}