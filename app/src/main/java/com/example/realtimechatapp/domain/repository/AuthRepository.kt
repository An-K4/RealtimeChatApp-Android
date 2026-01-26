package com.example.realtimechatapp.domain.repository

import com.example.realtimechatapp.domain.model.User

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<User>
}