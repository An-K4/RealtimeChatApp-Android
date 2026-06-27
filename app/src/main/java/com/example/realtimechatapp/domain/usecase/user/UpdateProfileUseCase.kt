package com.example.realtimechatapp.domain.usecase.user

import android.net.Uri
import com.example.realtimechatapp.domain.model.User
import com.example.realtimechatapp.domain.repository.MediaRepository
import com.example.realtimechatapp.domain.repository.UserRepository
import timber.log.Timber
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val mediaRepository: MediaRepository
) {
    suspend operator fun invoke(
        fullName: String,
        email: String,
        avatar: Uri?,
        isAvatarChanged: Boolean
    ): Result<User> {
        return try {
            val avatarUrl: String? = if (isAvatarChanged) {
                avatar?.let {
                    Timber.d("Đang tải lên avatar")
                    mediaRepository.upload(avatar).getOrThrow()
                }
            } else null
            Timber.d("Tải lên avatar thành công, url: %s", avatarUrl)

            userRepository.updateProfile(fullName, email, avatarUrl)
        } catch (e: Exception) {
            Timber.e(e, "Lỗi cập nhật thông tin")
            Result.failure(e)
        }
    }
}