package com.example.realtimechatapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.realtimechatapp.common.formatToTime
import com.example.realtimechatapp.domain.model.GroupMessageContact
import com.example.realtimechatapp.domain.model.LastMessage
import com.example.realtimechatapp.domain.model.MessageContact

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo("is_group")
    val isGroup: Boolean,

    @ColumnInfo("last_message")
    val lastMessage: String?,

    @ColumnInfo("last_sender_name")
    val lastSenderName: String?,

    @ColumnInfo("is_mine")
    val isMine: Boolean,

    @ColumnInfo("last_time_stamp")
    val lastTimeStamp: Long,

    @ColumnInfo("unread_count")
    val unreadCount: Int = 0,

    @ColumnInfo("contact_name")
    val contactName: String?,

    @ColumnInfo("contact_avatar")
    val contactAvatar: String?
)

fun ContactEntity.toMessageContact() = MessageContact(
    id = this.id,
    fullName = this.contactName ?: "",
    avatar = this.contactAvatar,
    unreadCount = this.unreadCount,
    lastMessage = LastMessage(
        content = this.lastMessage ?: "",
        createdAt = this.lastTimeStamp.formatToTime(true),
        senderName = this.lastSenderName,
        isMine = this.isMine
    ),
    lastMessageTime = this.lastTimeStamp.formatToTime(true)
)

fun ContactEntity.toGroupMessageContact() = GroupMessageContact(
    id = this.id,
    name = this.contactName ?: "",
    avatar = this.contactAvatar,
    description = "",
    ownerId = this.id,
    unreadCount = this.unreadCount,
    lastMessage = LastMessage(
        content = this.lastMessage ?: "",
        createdAt = this.lastTimeStamp.formatToTime(true),
        senderName = this.lastSenderName,
        isMine = this.isMine
    ),
    updatedAt = this.lastTimeStamp.formatToTime(true)
)