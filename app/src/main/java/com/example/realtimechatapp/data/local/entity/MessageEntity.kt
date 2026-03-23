package com.example.realtimechatapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["receiver_id"]),
        Index(value = ["group_id"]),
        Index(value = ["sender_id", "created_at"]),
        Index(value = ["reply_to_id"])
    ]
)
data class MessageEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "sender_id")
    val senderId: String,

    @ColumnInfo(name = "receiver_id")
    val receiverId: String? = null,

    @ColumnInfo(name = "group_id")
    val groupId: String? = null,

    val content: String?,

    @ColumnInfo(name = "reply_to_id")
    val replyToId: String? = null,

    val attachments: List<String>?,

    @ColumnInfo(name = "seen_by")
    val seenBy: List<String>?,

    val status: MessageStatus, // SENT, DELIVERED, READ

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

enum class MessageStatus{
    SENDING, SENT, DELIVERED, READ, FAILED
}
