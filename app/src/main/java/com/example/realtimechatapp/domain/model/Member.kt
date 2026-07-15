package com.example.realtimechatapp.domain.model

data class Member(
    val userId: User?,
    val role: Role = Role.MEMBER,
    val joinedAt: String
)