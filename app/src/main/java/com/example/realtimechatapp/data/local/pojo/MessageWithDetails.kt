package com.example.realtimechatapp.data.local.pojo

import androidx.room.Embedded
import androidx.room.Relation
import com.example.realtimechatapp.common.formatToTime
import com.example.realtimechatapp.data.local.entity.MessageEntity
import com.example.realtimechatapp.data.local.entity.UserEntity
import com.example.realtimechatapp.domain.model.Message

// map to Message on domain layer
data class MessageWithDetails(
    @Embedded val message: MessageEntity,

    @Relation(
        parentColumn = "sender_id",
        entityColumn = "id"
    )
    val sender: UserEntity?,

    @Relation(
        parentColumn = "reply_to_id",
        entityColumn = "id"
    )
    val messageReply: MessageEntity? = null
)

fun MessageWithDetails.toMessage() = Message(
    id = message.id,
    senderId = sender?.id ?: "",
    senderName = sender?.fullName,
    senderAvatar = sender?.avatar,
    receiverId = message.receiverId,
    groupId = message.groupId,
    content = message.content,
    replyToMessageId = message.replyToId,
    replyToContent = messageReply?.content,
    attachments = message.attachments,
    seenUserIds = message.seenBy,
    createdAt = message.createdAt.formatToTime(toHourMinute = true)
)
