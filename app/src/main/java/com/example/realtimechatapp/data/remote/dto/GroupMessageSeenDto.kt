package com.example.realtimechatapp.data.remote.dto

data class GroupMessageSeenDto (
    val messageId: String? = null,
    val groupId: String? = null,
    val userId: String? = null,
    val seenBy: List<String>? = null
)