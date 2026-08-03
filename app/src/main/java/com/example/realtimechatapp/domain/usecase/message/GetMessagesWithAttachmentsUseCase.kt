package com.example.realtimechatapp.domain.usecase.message

import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.repository.MessageRepository
import com.example.realtimechatapp.domain.validation.MessageValidator
import javax.inject.Inject

class GetMessagesWithAttachmentsUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(
        friendId: String,
        limit: Int = 30,
        offset: Int = 0
    ): Result<List<Message>> {
        return try {
            MessageValidator.validateMessageContactIdExist(friendId)
            messageRepository.getMessagesWithAttachments(friendId, limit, offset)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
