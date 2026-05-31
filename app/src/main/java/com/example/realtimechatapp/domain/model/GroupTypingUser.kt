package com.example.realtimechatapp.domain.model

data class GroupTypingUser(
    val groupId: String,
    val senderId: String,
    val senderName: String? = null
)
