package com.example.realtimechatapp.domain.usecase

import com.example.realtimechatapp.domain.repository.AuthRepository
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class SignupUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke(
        avatar: File?,
        username: String,
        password: String,
        fullName: String,
        email: String
    ): Result<String>{
        if(username.isBlank() || password.isBlank() || fullName.isBlank() || email.isBlank()){
            return Result.failure(Exception("Vui lòng nhập đầy đủ thông tin"))
        }

        val avatarUrl: String? = avatar?.let {
            Timber.d("Đang tải lên")
            authRepository.uploadAvatar(avatar).getOrNull()
        }

        Timber.d("Tải hoàn tất, url là %s", avatarUrl)
        return authRepository.signup(username, password, fullName, email, avatarUrl)
    }
}