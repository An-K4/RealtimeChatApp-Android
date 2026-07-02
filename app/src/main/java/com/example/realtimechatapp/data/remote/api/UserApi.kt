package com.example.realtimechatapp.data.remote.api

import com.example.realtimechatapp.data.remote.dto.user.ChangePasswordRequestDto
import com.example.realtimechatapp.data.remote.dto.MessageResponseDto
import com.example.realtimechatapp.data.remote.dto.user.SearchResponseDto
import com.example.realtimechatapp.data.remote.dto.user.UpdateProfileRequestDto
import com.example.realtimechatapp.data.remote.dto.user.UserResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface UserApi {
    @PATCH("/users/update")
    suspend fun updateProfile(@Body requestBody: UpdateProfileRequestDto): UserResponseDto

    @POST("/users/change-password")
    suspend fun changePassword(@Body requestBody: ChangePasswordRequestDto): MessageResponseDto

    @GET("/users/search")
    suspend fun performSearch(@Query("keyword") keyword: String): SearchResponseDto

    @GET("/users/search-users")
    suspend fun performSearchUsers(@Query("keyword") keyword: String): SearchResponseDto
}