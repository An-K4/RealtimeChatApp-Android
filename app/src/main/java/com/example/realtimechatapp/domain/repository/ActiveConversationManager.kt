package com.example.realtimechatapp.domain.repository

import com.example.realtimechatapp.domain.model.ActiveConversation
import com.example.realtimechatapp.domain.model.ConversationType
import kotlinx.coroutines.flow.Flow

interface ActiveConversationManager {
    fun getActiveConversation(): Flow<ActiveConversation?>
    suspend fun setActiveConversation(conversationId: String, type: ConversationType)
    suspend fun clearActiveConversation()
}
