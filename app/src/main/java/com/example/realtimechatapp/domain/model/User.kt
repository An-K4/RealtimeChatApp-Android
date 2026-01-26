package com.example.realtimechatapp.domain.model

data class User(
    val id: String,
    val username: String,
    val fullName: String,
    val email: String,
    val avatar: String?,
    val createdAt: String
)