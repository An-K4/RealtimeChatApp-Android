package com.example.realtimechatapp.domain.model

import com.google.gson.annotations.SerializedName

enum class Role{
    // OWNER,
    ADMIN,
    MEMBER
}
data class Member(
    val userId: User,
    val role: Role = Role.MEMBER,
    val joinedAt: String
)