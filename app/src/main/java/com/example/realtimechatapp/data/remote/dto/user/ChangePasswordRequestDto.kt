package com.example.realtimechatapp.data.remote.dto.user

data class ChangePasswordRequestDto(
    val oldPassword: String,
    val newPassword: String
)
