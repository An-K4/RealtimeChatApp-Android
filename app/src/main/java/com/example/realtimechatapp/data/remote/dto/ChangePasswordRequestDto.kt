package com.example.realtimechatapp.data.remote.dto

data class ChangePasswordRequestDto(
    val oldPassword: String,
    val newPassword: String
)
