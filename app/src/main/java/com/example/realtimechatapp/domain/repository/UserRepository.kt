package com.example.realtimechatapp.domain.repository

import com.example.realtimechatapp.domain.model.User
import java.io.File

interface UserRepository {
    suspend fun updateProfile(fullName: String, email: String): Result<User>
    suspend fun updateAvatar(file: File): Result<String>
    suspend fun changePassword(oldPassword: String, newPassword: String): Result<String>
}