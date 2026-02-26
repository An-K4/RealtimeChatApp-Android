package com.example.realtimechatapp.domain.repository

import com.example.realtimechatapp.domain.model.User
import java.io.File

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<User>
    suspend fun uploadAvatar(file: File): Result<String?>
    suspend fun signup(username: String, password: String, fullName: String, email: String, avatar: String?): Result<String>
    suspend fun logout(): Result<String>
    suspend fun getMe(): Result<User>
}