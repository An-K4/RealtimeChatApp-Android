package com.example.realtimechatapp.domain.usecase.socket

import com.example.realtimechatapp.domain.model.MessageContact
import com.example.realtimechatapp.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMessageContactUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    operator fun invoke(): Flow<List<MessageContact>>{
        return messageRepository.observeMessageContacts()
    }
}