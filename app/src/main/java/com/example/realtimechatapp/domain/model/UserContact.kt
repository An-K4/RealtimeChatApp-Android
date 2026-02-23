package com.example.realtimechatapp.domain.model

data class UserContact(
    val id: String,
    val username: String,
    val fullName: String,
    val email: String,
    val avatar: String?,
    val unreadCount: Int,
    val lastMessage: LastMessage,
    val lastMessageTime: String
)

data class LastMessage(
    val content: String,
    val createdAt: String,
    val isMine: Boolean
)