package com.example.realtimechatapp.domain.usecase.messages

import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.repository.MessageRepository
import javax.inject.Inject

class GetMessageUseCase @Inject constructor(private val messageRepository: MessageRepository) {
    suspend operator fun invoke(friendId: String): Result<List<Message>>{
        return messageRepository.getMessage(friendId)
    }
}