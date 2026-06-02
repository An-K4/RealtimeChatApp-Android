package com.example.realtimechatapp.domain.usecase.message

import com.example.realtimechatapp.domain.repository.MessageRepository
import javax.inject.Inject

class GetMessageUseCase @Inject constructor(private val messageRepository: MessageRepository) {
    suspend operator fun invoke(friendId: String): Result<Unit>{
        // contact id is friend id that current user chat with
        return try {
            messageRepository.getMessage(friendId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}