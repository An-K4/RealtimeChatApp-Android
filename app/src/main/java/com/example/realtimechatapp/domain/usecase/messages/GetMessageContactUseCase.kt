package com.example.realtimechatapp.domain.usecase.messages

import com.example.realtimechatapp.domain.model.MessageContact
import com.example.realtimechatapp.domain.repository.MessageRepository
import javax.inject.Inject

class GetMessageContactUseCase @Inject constructor(private val messageRepository: MessageRepository) {
    suspend operator fun invoke(): Result<List<MessageContact>> {
        return messageRepository.getUsers()
    }
}