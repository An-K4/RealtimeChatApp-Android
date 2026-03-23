package com.example.realtimechatapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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

    @ColumnInfo("last_time_stamp")
    val lastTimeStamp: Long,

    @ColumnInfo("unread_count")
    val unreadCount: Int = 0,

    @ColumnInfo("contact_name")
    val contactName: String?,

    @ColumnInfo("contact_avatar")
    val contactAvatar: String?
)
