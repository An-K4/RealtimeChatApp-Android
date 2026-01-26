package com.example.realtimechatapp.data.repository

import com.example.realtimechatapp.data.remote.AuthApi
import com.example.realtimechatapp.data.remote.dto.LoginRequestDto
import com.example.realtimechatapp.domain.model.User
import com.example.realtimechatapp.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(private val api: AuthApi): AuthRepository {
    override suspend fun login(
        username: String,
        password: String
    ): Result<User> {
        return try {
            val response = api.login(LoginRequestDto(username, password))
            val user = response.user.toUser()
            Result.success(user)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}