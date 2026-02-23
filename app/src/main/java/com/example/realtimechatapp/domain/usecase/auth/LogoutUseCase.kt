package com.example.realtimechatapp.domain.usecase.auth

import com.example.realtimechatapp.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke(token: String) : Result<String>{
        return authRepository.logout(token)
    }
}