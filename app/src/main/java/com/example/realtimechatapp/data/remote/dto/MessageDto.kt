package com.example.realtimechatapp.data.remote.dto

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
    fun toMessage(): Message {
        val senderId = senderId.toUser()
        val receiverId = receiverId?.toUser()

        return Message(
            id = id,
            senderId = senderId.id,
            senderName = senderId.fullName,
            senderAvatar = senderId.avatar,
            receiverId = receiverId?.id,
            groupId = groupId?.toGroup()?.id,
            content = content,
            replyToMessageId = replyTo?.id,
            replyToContent = replyTo?.content,
            attachments = attachments,
            seenUserIds = seenBy?.map { it.id },
            createdAt = createdAt
        )
    }
}
