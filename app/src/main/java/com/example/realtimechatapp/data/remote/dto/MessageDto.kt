package com.example.realtimechatapp.data.remote.dto

import com.example.realtimechatapp.common.isoToLong
import com.example.realtimechatapp.data.local.entity.MessageEntity
import com.example.realtimechatapp.domain.model.Message
import com.google.gson.annotations.SerializedName

data class MessageDto(
    @SerializedName("_id") val id: String,
    val senderId: UserDto,
    val receiverId: UserDto?,
    val groupId: GroupDto?,
    val content: String?,
    val replyTo: MessageDto?,
    val attachments: String?,
    val seenBy: List<UserDto>?,
    val createdAt: String
){
    fun toMessageEntity() = MessageEntity(
        id = this.id,
        senderId = this.senderId.id,
        receiverId = this.receiverId?.id,
        content = content,
        replyToId = replyTo?.id,
        attachments = attachments,
        seenBy = seenBy?.map { it.id },
        createdAt = createdAt.isoToLong()
    )
}
