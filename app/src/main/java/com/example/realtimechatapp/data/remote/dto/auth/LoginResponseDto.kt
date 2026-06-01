package com.example.realtimechatapp.data.remote.dto.auth

import com.example.realtimechatapp.data.remote.dto.user.UserDto

data class LoginResponseDto(
    val message: String,
    val token: String,
    val user: UserDto
)