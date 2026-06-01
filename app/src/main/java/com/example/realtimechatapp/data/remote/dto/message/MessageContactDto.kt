package com.example.realtimechatapp.data.remote.dto.message

import com.example.realtimechatapp.common.isoToLong
import com.example.realtimechatapp.data.local.entity.ContactEntity
import com.example.realtimechatapp.data.local.entity.UserEntity
import com.google.gson.annotations.SerializedName

data class MessageContactDto(
    @SerializedName("_id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("avatar") val avatar: String?,
    @SerializedName("unreadCount") val unreadCount: Int,
    @SerializedName("lastMessage") val lastMessage: LastMessageDto,
    @SerializedName("lastMessageTime") val lastMessageTime: String
){
    fun toUserEntity(): UserEntity{
        return UserEntity(
            id = this.id,
            username = this.username,
            fullName = this.fullName,
            email = this.email,
            avatar = this.avatar
        )
    }

    fun toMessageContactEntity(): ContactEntity{
        return ContactEntity(
            id = this.id,
            isGroup = false,
            lastMessage = this.lastMessage.content,
            lastSenderName = this.lastMessage.senderName,
            lastTimeStamp = this.lastMessageTime.isoToLong(),
            unreadCount = this.unreadCount,
            contactName = this.fullName,
            contactAvatar = this.avatar,
            isMine = this.lastMessage.isMine
        )
    }
}
