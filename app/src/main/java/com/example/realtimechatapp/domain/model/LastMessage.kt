package com.example.realtimechatapp.domain.model

// use to render user message list
data class LastMessage(
    val content: String,
    val createdAt: String,
    val senderName: String?,
    val isMine: Boolean
)