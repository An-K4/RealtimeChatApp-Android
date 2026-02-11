package com.example.realtimechatapp.data.remote.dto

data class SignupRequestDto(
    val username: String,
    val password: String,
    val fullName: String,
    val email: String,
    val avatar: String?
)