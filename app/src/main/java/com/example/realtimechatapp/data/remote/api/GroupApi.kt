package com.example.realtimechatapp.data.remote.api

import com.example.realtimechatapp.data.remote.dto.group.CreateGroupRequestDto
import com.example.realtimechatapp.data.remote.dto.group.CreateGroupResponseDto
import com.example.realtimechatapp.data.remote.dto.group.GetGroupInfoResponseDto
import com.example.realtimechatapp.data.remote.dto.group.GetGroupMessageResponseDto
import com.example.realtimechatapp.data.remote.dto.group.GetGroupResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface GroupApi {
    @GET("/groups/getGroups")
    suspend fun getGroups(): GetGroupResponseDto

    @GET("groups/{id}/messages")
    suspend fun getGroupMessage(@Path("id") groupId: String): GetGroupMessageResponseDto

    @GET("/groups/{id}")
    suspend fun getGroupInfo(@Path("id") groupId: String): GetGroupInfoResponseDto

    @POST("/groups/create")
    suspend fun createGroup(@Body request: CreateGroupRequestDto): CreateGroupResponseDto
}