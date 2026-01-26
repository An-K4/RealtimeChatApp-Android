package com.example.realtimechatapp.domain.model

import java.util.Date

data class Message(
    val id: String,

    // sender
    val senderId: String,
    val senderName: String,
    val senderAvatar: String,

    val receiverId: String?,
    val groupId: String?,
    val content: String,

    // reply
    val replyToMessageId: String?,
    val replyToContent: String?,

    val attachments: String?,
    val seenUserIds: List<String>,
    val createdAt: Date
)
