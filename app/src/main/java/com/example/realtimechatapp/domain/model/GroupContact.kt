package com.example.realtimechatapp.domain.model

data class GroupContact(
    val id: String,
    val name: String,
    val avatar: String?,
    val description: String,
    val ownerId: String,
    val unreadCount: Int,
    val lastMessage: LastMessage,
    val updatedAt: String
)
