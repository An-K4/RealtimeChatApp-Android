package com.example.realtimechatapp.domain.usecase.user

import com.example.realtimechatapp.domain.repository.UserRepository
import java.io.File
import javax.inject.Inject

class UpdateAvatarUseCase @Inject constructor(private val userRepository: UserRepository) {
    suspend operator fun invoke(avatar: File): Result<String> {
        return userRepository.updateAvatar(avatar)
    }
}