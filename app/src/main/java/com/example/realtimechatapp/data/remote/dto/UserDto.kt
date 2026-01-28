package com.example.realtimechatapp.data.remote.dto

import com.example.realtimechatapp.domain.model.User
import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("_id") val id: String,
    val username: String,
    val fullName: String,
    val email: String,
    val avatar: String?,
    val createdAt: String
) {
    fun toUser(): User{
        return User(
            id = id,
            username = username,
            fullName = fullName,
            email = email,
            avatar = avatar,
            createdAt = createdAt
        )
    }
}