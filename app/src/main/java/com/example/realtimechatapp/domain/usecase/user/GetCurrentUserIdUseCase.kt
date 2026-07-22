package com.example.realtimechatapp.domain.usecase.user

import com.example.realtimechatapp.domain.exception.AuthException
import com.example.realtimechatapp.domain.repository.CurrentUserManager
import javax.inject.Inject

class GetCurrentUserIdUseCase @Inject constructor(
    private val currentUserManager: CurrentUserManager
) {
    suspend operator fun invoke(): Result<String> {
        return try {
            val currentUserId = currentUserManager.getCurrentUserId()
            if (currentUserId != null) {
                Result.success(currentUserId)
            } else {
                Result.failure(AuthException.InvalidCurrentUserIdException)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}