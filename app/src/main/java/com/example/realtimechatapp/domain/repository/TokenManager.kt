package com.example.realtimechatapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface TokenManager {
    // Access Token
    suspend fun saveToken(token: String)
    val token: Flow<String?>
    suspend fun deleteToken()
    
    // Refresh Token
    suspend fun saveRefreshToken(token: String)
    val refreshToken: Flow<String?>
    suspend fun deleteRefreshToken()
}