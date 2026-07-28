package com.example.realtimechatapp.data.remote.dto.auth

data class RefreshTokenResponseDto(
    val message: String,
    val accessToken: String,
    val refreshToken: String
)
