package com.example.realtimechatapp.data.remote

import com.example.realtimechatapp.data.remote.dto.GetGroupResponseDto
import retrofit2.http.GET

interface GroupApi {
    @GET("/groups/getGroups")
    suspend fun getGroups(): GetGroupResponseDto
}