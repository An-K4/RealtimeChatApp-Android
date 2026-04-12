package com.example.realtimechatapp.domain.usecase.socket

import com.example.realtimechatapp.data.remote.dto.MessageSeenDto
import com.example.realtimechatapp.domain.repository.MessageRepository
import com.example.realtimechatapp.domain.repository.SocketRepository
import javax.inject.Inject

class SeenMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(friendId: String){
        messageRepository.seenMessage(friendId)
    }
}