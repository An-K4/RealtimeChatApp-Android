package com.example.realtimechatapp.domain.usecase.conversation

import com.example.realtimechatapp.domain.model.ConversationType
import com.example.realtimechatapp.domain.repository.ActiveConversationManager
import javax.inject.Inject

class SetActiveConversationUseCase @Inject constructor(
    private val activeConversationManager: ActiveConversationManager
) {
    suspend operator fun invoke(conversationId: String, type: ConversationType) {
        activeConversationManager.setActiveConversation(conversationId, type)
    }
}
