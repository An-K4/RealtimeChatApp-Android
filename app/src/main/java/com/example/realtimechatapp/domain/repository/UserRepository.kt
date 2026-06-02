package com.example.realtimechatapp.domain.repository

import android.net.Uri
import com.example.realtimechatapp.domain.model.SearchResult
import com.example.realtimechatapp.domain.model.User

interface UserRepository {
    suspend fun updateProfile(fullName: String, email: String): Result<User>
    suspend fun updateAvatar(avatar: Uri): Result<String>
    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit>
    suspend fun performSearch(query: String): Result<SearchResult>
}