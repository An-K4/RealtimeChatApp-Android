package com.example.realtimechatapp.domain.model

data class MessageContact(
    val id: String,
    val avatar: String?,
    val fullName: String,
    val unreadCount: Int,
    val lastMessage: LastMessage,
    val lastMessageTime: String,
    val isOnline: Boolean = false,
    val isTyping: Boolean = false
)