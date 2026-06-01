package com.example.realtimechatapp.domain.usecase.message

import com.example.realtimechatapp.domain.repository.MessageRepository
import javax.inject.Inject

class GetMessageContactUseCase @Inject constructor(private val messageRepository: MessageRepository) {
    suspend operator fun invoke(): Result<Unit> {
        return messageRepository.getMessageContacts()
    }
}