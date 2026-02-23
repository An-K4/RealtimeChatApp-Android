package com.example.realtimechatapp.domain.usecase.messages

import com.example.realtimechatapp.domain.model.UserContact
import com.example.realtimechatapp.domain.repository.MessageRepository
import javax.inject.Inject

class GetUsersUseCase @Inject constructor(private val messageRepository: MessageRepository) {
    suspend operator fun invoke(): Result<List<UserContact>> {
        return messageRepository.getUsers()
    }
}