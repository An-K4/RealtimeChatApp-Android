package com.example.realtimechatapp.data.remote.dto

data class MessageSeenDto(
    val senderId: String? = null,
    val viewerId: String? = null,
    val sentAt: String? = null
)
