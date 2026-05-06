package com.example.realtimechatapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface TokenManager {
    suspend fun saveToken(token: String)
    val token: Flow<String?>
    suspend fun deleteToken()
}