package com.example.realtimechatapp.domain.usecase.auth

import android.net.Uri
import com.example.realtimechatapp.domain.repository.AuthRepository
import com.example.realtimechatapp.domain.validation.AuthValidator
import timber.log.Timber
import javax.inject.Inject

class SignupUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke(
        avatar: Uri?,
        username: String,
        password: String,
        passwordRetype: String,
        fullName: String,
        email: String
    ): Result<Unit>{
        return try {
            AuthValidator.validateSignUp(username, password, passwordRetype, fullName, email)

            val avatarUrl: String? = avatar?.let {
                Timber.d("Đang tải lên avatar")
                authRepository.uploadAvatar(avatar).getOrThrow()
            }
            Timber.d("Tải lên avatar thành công, url: %s", avatarUrl)

            authRepository.signup(username, password, fullName, email, avatarUrl)
        } catch (e: Exception) {
            Timber.e(e, "Lỗi đăng ký tài khoản")
            Result.failure(e)
        }
    }
}