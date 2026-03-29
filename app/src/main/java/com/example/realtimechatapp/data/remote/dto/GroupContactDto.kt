package com.example.realtimechatapp.data.remote.dto

import com.example.realtimechatapp.common.isoToLong
import com.example.realtimechatapp.data.local.entity.ContactEntity
import com.example.realtimechatapp.data.local.entity.GroupEntity
import com.example.realtimechatapp.domain.model.GroupContact
import com.google.gson.annotations.SerializedName

data class GroupContactDto(
    @SerializedName("_id") val id: String,
    val name: String,
    val avatar: String?,
    val description: String,
    @SerializedName("owner") val ownerId: String,
    val unreadCount: Int,
    val lastMessage: LastMessageDto,
    val updatedAt: String
){
    fun toContactEntity(): ContactEntity{
        return ContactEntity(
            id = this.id,
            isGroup = true,
            lastMessage = this.lastMessage.content,
            lastSenderName = this.lastMessage.senderName,
            isMine = this.lastMessage.isMine,
            lastTimeStamp = this.lastMessage.createdAt.isoToLong(),
            unreadCount = this.unreadCount,
            contactName = this.name,
            contactAvatar = this.avatar
        )
    }
}
