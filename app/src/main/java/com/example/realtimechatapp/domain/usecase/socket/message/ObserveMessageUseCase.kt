package com.example.realtimechatapp.domain.usecase.socket.message

import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    operator fun invoke(friendId: String): Flow<List<Message>> = messageRepository.observeMessages(friendId)
}