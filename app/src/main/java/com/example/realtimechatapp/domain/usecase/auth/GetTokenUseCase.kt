package com.example.realtimechatapp.domain.usecase.auth

import com.example.realtimechatapp.domain.exception.AuthException
import com.example.realtimechatapp.domain.repository.TokenManager
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetTokenUseCase @Inject constructor(
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(): Result<String?> {
        return try {
            val token = tokenManager.token.first()
            if (!token.isNullOrEmpty()) {
                Result.success(token)
            } else {
                throw AuthException.UnauthorizedException
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}