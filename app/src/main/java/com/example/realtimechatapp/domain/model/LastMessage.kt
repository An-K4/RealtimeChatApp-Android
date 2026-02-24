package com.example.realtimechatapp.domain.model

data class LastMessage(
    val content: String,
    val createdAt: String,
    val senderName: String?,
    val isMine: Boolean
)