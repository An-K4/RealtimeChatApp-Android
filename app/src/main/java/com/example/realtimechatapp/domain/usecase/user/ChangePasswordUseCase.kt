package com.example.realtimechatapp.domain.usecase.user

import com.example.realtimechatapp.domain.repository.UserRepository
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(private val userRepository: UserRepository) {
    suspend operator fun invoke(oldPassword: String, newPassword: String): Result<String>{
        if (oldPassword == newPassword){
            return Result.failure(Exception("Mật khẩu mới không được trùng với mật khẩu cũ."))
        }

        return userRepository.changePassword(oldPassword, newPassword)
    }
}