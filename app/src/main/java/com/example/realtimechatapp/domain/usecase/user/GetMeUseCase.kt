package com.example.realtimechatapp.domain.usecase.user

import com.example.realtimechatapp.domain.model.User
import com.example.realtimechatapp.domain.repository.AuthRepository
import javax.inject.Inject

class GetMeUseCase @Inject constructor(private val authRepository: AuthRepository){
    suspend operator fun invoke(): Result<User>{
        return authRepository.getMe()
    }
}