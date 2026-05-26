package com.example.realtimechatapp.data.remote.dto

import com.example.realtimechatapp.common.isoToLong
import com.example.realtimechatapp.data.local.entity.MessageEntity
import com.google.gson.annotations.SerializedName

data class MessageDto(
    @SerializedName("_id") val id: String,
    val senderId: UserDto,
    val receiverId: UserDto?,
    val groupId: String?,
    val content: String?,
    val replyTo: MessageDto?,
    val attachments: String?,
    val seenBy: List<UserDto>?,
    val createdAt: String
) {
    fun toMessageEntity() = MessageEntity(
        id = this.id,
        senderId = this.senderId.id,
        receiverId = this.receiverId?.id,
        groupId = this.groupId,
        content = this.content,
        replyToId = this.replyTo?.id,
        attachments = this.attachments,
        seenBy = this.seenBy?.map { it.id },
        createdAt = this.createdAt.isoToLong()
    )

    fun getMessageContactId(currentUserId: String): String {
        return when {
            groupId != null -> groupId
            receiverId != null -> {
                if (senderId.id == currentUserId){
                    receiverId.id
                } else {
                    senderId.id
                }
            }
            else -> senderId.id
        }
    }
}
