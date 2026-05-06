package com.example.realtimechatapp.domain.usecase.messages

import com.example.realtimechatapp.domain.model.User
import com.example.realtimechatapp.domain.repository.MessageRepository
import com.example.realtimechatapp.domain.validation.MessageValidator
import javax.inject.Inject

class GetHeaderInfoUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(userId: String): Result<User>{
        // contact id is user id that current user chat with
        return try {
            MessageValidator.validateMessageContactIdExist(userId)
            messageRepository.getHeaderInfo(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}