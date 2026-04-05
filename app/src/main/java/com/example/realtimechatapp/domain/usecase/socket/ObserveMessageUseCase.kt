package com.example.realtimechatapp.domain.usecase.socket

import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

class ObserveMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    operator fun invoke(friendId: String): Flow<List<Message>> {
        return messageRepository.observeMessage(friendId)
    }
}