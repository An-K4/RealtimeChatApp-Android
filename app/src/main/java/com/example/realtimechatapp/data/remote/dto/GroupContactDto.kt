package com.example.realtimechatapp.data.remote.dto

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
    fun toGroup(): GroupContact{
        return GroupContact(
            id = this.id,
            name = this.name,
            avatar = this.avatar,
            description = this.description,
            ownerId = this.ownerId,
            unreadCount = this.unreadCount,
            lastMessage = this.lastMessage.toLastMessage(),
            updatedAt = this.updatedAt
        )
    }
}
