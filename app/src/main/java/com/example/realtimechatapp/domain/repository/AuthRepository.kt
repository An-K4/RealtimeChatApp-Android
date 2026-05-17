package com.example.realtimechatapp.domain.repository

import android.net.Uri
import com.example.realtimechatapp.domain.model.User

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<User>
    suspend fun uploadAvatar(file: Uri): Result<String?>
    suspend fun signup(username: String, password: String, fullName: String, email: String, avatar: String?): Result<Unit>
    suspend fun logout(): Result<Unit>
    suspend fun getMe(): Result<User>
}