package com.example.realtimechatapp.domain.usecase.user

import com.example.realtimechatapp.domain.model.User
import com.example.realtimechatapp.domain.repository.UserRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(private val userRepository: UserRepository) {
    suspend operator fun invoke(fullName: String, email: String): Result<User>{
        return userRepository.updateProfile(fullName, email)
    }
}