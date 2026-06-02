package com.example.realtimechatapp.domain.usecase.message

import com.example.realtimechatapp.domain.model.User
import com.example.realtimechatapp.domain.repository.MessageRepository
import javax.inject.Inject

class GetHeaderInfoUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(userId: String): Result<User>{
        // contact id is user id that current user chat with
        return try {
            messageRepository.getHeaderInfo(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}