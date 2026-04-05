package com.example.realtimechatapp.data.remote.api

import com.example.realtimechatapp.data.remote.dto.GetGroupInfoResponseDto
import com.example.realtimechatapp.data.remote.dto.GetGroupMessageResponseDto
import com.example.realtimechatapp.data.remote.dto.GetGroupResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

interface GroupApi {
    @GET("/groups/getGroups")
    suspend fun getGroups(): GetGroupResponseDto

    @GET("groups/{id}/messages")
    suspend fun getGroupMessage(@Path("id") groupId: String): GetGroupMessageResponseDto

    @GET("/groups/{id}")
    suspend fun getGroupInfo(@Path("id") groupId: String): GetGroupInfoResponseDto
}