package com.example.realtimechatapp.domain.model

import com.google.gson.annotations.SerializedName
import java.util.Date

enum class Role{
    // @SerializedName("owner") OWNER,
    @SerializedName("admin") ADMIN,
    @SerializedName("member") MEMBER
}
data class Member(
    val userId: User,
    val role: Role = Role.MEMBER,
    val joinedAt: Date
)