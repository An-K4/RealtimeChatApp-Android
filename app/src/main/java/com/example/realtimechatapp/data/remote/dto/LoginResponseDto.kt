package com.example.realtimechatapp.data.remote.dto

data class LoginResponseDto(
    val message: String,
    val token: String,
    val user: UserDto
)