package com.example.realtimechatapp.domain.usecase.socket.message

import com.example.realtimechatapp.domain.repository.MessageRepository
import javax.inject.Inject

class SeenMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(friendId: String){
        messageRepository.seenMessage(friendId)
    }
}