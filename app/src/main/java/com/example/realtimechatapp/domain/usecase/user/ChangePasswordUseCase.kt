package com.example.realtimechatapp.domain.usecase.user

import com.example.realtimechatapp.domain.repository.UserRepository
import com.example.realtimechatapp.domain.validation.AuthValidator
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(private val userRepository: UserRepository) {
    suspend operator fun invoke(
        oldPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): Result<Unit> {
        return try {
            AuthValidator.validatePasswordMatch(newPassword, confirmNewPassword)
            userRepository.changePassword(oldPassword, newPassword)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}