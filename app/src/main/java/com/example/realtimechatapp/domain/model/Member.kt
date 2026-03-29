package com.example.realtimechatapp.domain.model

enum class Role{
    // OWNER,
    ADMIN,
    MEMBER,
    OWNER
}
data class Member(
    val userId: User,
    val role: Role = Role.MEMBER,
    val joinedAt: String
)