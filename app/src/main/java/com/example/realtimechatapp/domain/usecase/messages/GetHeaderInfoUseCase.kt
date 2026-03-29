package com.example.realtimechatapp.domain.usecase.messages

import com.example.realtimechatapp.domain.model.User
import com.example.realtimechatapp.domain.repository.MessageRepository
import javax.inject.Inject

class GetHeaderInfoUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(userId: String): Result<User>{
        return messageRepository.getHeaderInfo(userId)
    }
}