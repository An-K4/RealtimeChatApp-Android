package com.example.realtimechatapp.domain.model

data class ActiveConversation(
    val conversationId: String,  // Contact ID or Group ID
    val type: ConversationType
)
