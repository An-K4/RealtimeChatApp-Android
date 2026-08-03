package com.example.realtimechatapp.domain.usecase.conversation

import com.example.realtimechatapp.domain.repository.ActiveConversationManager
import javax.inject.Inject

class ClearActiveConversationUseCase @Inject constructor(
    private val activeConversationManager: ActiveConversationManager
) {
    suspend operator fun invoke() {
        activeConversationManager.clearActiveConversation()
    }
}
