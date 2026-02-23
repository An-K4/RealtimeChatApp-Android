package com.example.realtimechatapp.data.remote.dto

import com.example.realtimechatapp.domain.model.LastMessage
import com.example.realtimechatapp.domain.model.UserContact
import com.google.gson.annotations.SerializedName

data class UserContactDto(
    @SerializedName("_id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("avatar") val avatar: String?,
    @SerializedName("unreadCount") val unreadCount: Int,
    @SerializedName("lastMessage") val lastMessage: LastMessageDto,
    @SerializedName("lastMessageTime") val lastMessageTime: String
){
    fun toUserContact(): UserContact{
        return UserContact(
            id = this.id,
            username = this.username,
            fullName = this.fullName,
            email = this.email,
            avatar = this.avatar,
            unreadCount = this.unreadCount,
            lastMessage = LastMessage(
                content = this.lastMessage.content,
                createdAt = this.lastMessage.createdAt,
                isMine = this.lastMessage.isMine
            ),
            lastMessageTime = this.lastMessageTime
        )
    }
}

data class LastMessageDto(
    val content: String,
    val createdAt: String,
    val isMine: Boolean
)
