package com.example.realtimechatapp.data.remote.dto

import com.example.realtimechatapp.common.formatToTime
import com.example.realtimechatapp.common.isoToLong
import com.example.realtimechatapp.data.local.entity.UserEntity
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
    fun toUser(): User {
        return User(
            id = this.id,
            username = this.username,
            fullName = this.fullName,
            email = this.email,
            avatar = this.avatar?.takeIf { it.isNotBlank() },
            createdAt = this.createdAt.formatToTime(false)
        )
    }

    fun toUserEntity() = UserEntity(
        id = this.id,
        username = this.username,
        fullName = this.fullName,
        email = this.email,
        avatar = avatar,
        createdAt = this.createdAt.isoToLong()
    )
}