package com.example.realtimechatapp.domain.usecase.user

import com.example.realtimechatapp.common.ImageUtils
import com.example.realtimechatapp.domain.repository.UserRepository
import com.example.realtimechatapp.domain.validation.UserValidator
import java.io.File
import javax.inject.Inject

class UpdateAvatarUseCase @Inject constructor(private val userRepository: UserRepository) {
    suspend operator fun invoke(avatarFile: File?): Result<String> {
        return try {
            UserValidator.validateFile(avatarFile)
            val compressedAvatar = ImageUtils.compressImageFile(avatarFile!!)
            UserValidator.validateCompressedAvatar(compressedAvatar)
            userRepository.updateAvatar(compressedAvatar!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}