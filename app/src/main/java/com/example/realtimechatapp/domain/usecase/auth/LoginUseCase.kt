package com.example.realtimechatapp.domain.usecase.auth

import com.example.realtimechatapp.domain.model.User
import com.example.realtimechatapp.domain.repository.AuthRepository
import com.example.realtimechatapp.domain.validation.AuthValidator
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke(username: String, password: String): Result<User>{
        return try {
            AuthValidator.validateLogin(username, password)
            authRepository.login(username, password)
        } catch (e: Exception){
            Result.failure(e)
        }
    }
}