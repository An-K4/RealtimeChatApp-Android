package com.example.realtimechatapp.domain.usecase.user

import android.net.Uri
import com.example.realtimechatapp.domain.repository.UserRepository
import javax.inject.Inject

class UpdateAvatarUseCase @Inject constructor(private val userRepository: UserRepository) {
    suspend operator fun invoke(avatar: Uri): Result<String> {
        return userRepository.updateAvatar(avatar)
    }
}