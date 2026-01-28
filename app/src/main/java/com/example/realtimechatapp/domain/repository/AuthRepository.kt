package com.example.realtimechatapp.domain.repository

import com.example.realtimechatapp.data.remote.dto.LogoutResponseDto
import com.example.realtimechatapp.domain.model.User

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<User>
    suspend fun signup(username: String, password: String, fullName: String, email: String): Result<String>
    suspend fun logout(token: String): Result<String>
}