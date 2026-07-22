package com.example.realtimechatapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface CurrentUserManager {
    suspend fun getCurrentUserId(): String?
    suspend fun switchUser(userId: String)
    fun observeCurrentUser(): Flow<String?>
}