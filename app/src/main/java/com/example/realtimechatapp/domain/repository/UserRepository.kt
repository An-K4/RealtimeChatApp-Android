package com.example.realtimechatapp.domain.repository

import com.example.realtimechatapp.domain.model.SearchResult
import com.example.realtimechatapp.domain.model.User

interface UserRepository {
    suspend fun updateProfile(fullName: String, email: String, avatar: String?): Result<User>
    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit>
    suspend fun performSearch(query: String): Result<SearchResult>
    suspend fun performSearchUsers(query: String): Result<SearchResult>
    suspend fun saveNewUserInfo(newUser: User): Result<Unit>
    suspend fun getOtherLocalUsers(): Result<List<User>>
    suspend fun getUserWithMutedStatus(userId: String): Result<User>
    
    // FCM Token Management
    suspend fun updateFcmToken(token: String)
    suspend fun getCurrentFcmToken(): String?
    suspend fun syncFcmTokenToServer(token: String): Result<Unit>
}