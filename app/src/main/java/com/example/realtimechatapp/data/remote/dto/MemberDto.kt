package com.example.realtimechatapp.data.remote.dto

import com.example.realtimechatapp.domain.model.Member
import com.example.realtimechatapp.domain.model.Role
import com.google.gson.annotations.SerializedName

enum class RoleDto{
    @SerializedName("admin") ADMIN,
    @SerializedName("member") MEMBER;

    fun toRole(): Role{
        return when(this){
            ADMIN -> Role.ADMIN
            MEMBER -> Role.MEMBER
        }
    }
}

data class MemberDto(
    val userId: UserDto,
    val role: RoleDto,
    val joinedAt: String
){
    fun toMember(): Member{
        return Member(
            userId = userId.toUser(),
            role = role.toRole(),
            joinedAt = joinedAt
        )
    }
}
