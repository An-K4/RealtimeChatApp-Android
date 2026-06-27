package com.example.realtimechatapp.data.remote.dto.user

data class UpdateProfileRequestDto(
    val fullName: String,
    val email: String,
    val avatar: String?
)
