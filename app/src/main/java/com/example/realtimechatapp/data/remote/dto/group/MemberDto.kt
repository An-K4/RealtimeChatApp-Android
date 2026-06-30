package com.example.realtimechatapp.data.remote.dto.group

import com.example.realtimechatapp.common.isoToLong
import com.example.realtimechatapp.data.local.entity.MemberEntity
import com.example.realtimechatapp.data.local.entity.MemberRole
import com.example.realtimechatapp.data.remote.dto.user.UserDto
import com.example.realtimechatapp.domain.model.Member
import com.example.realtimechatapp.domain.model.Role
import com.google.gson.annotations.SerializedName

enum class RoleDto{
    @SerializedName("admin") ADMIN,
    @SerializedName("member") MEMBER;

    fun toMemberRole(): MemberRole{
        return when(this){
            ADMIN -> MemberRole.ADMIN
            MEMBER -> MemberRole.MEMBER
        }
    }

    fun toRole(): Role {
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
    fun toMember() = Member(
        userId = userId.toUser(),
        role = role.toRole(),
        joinedAt = joinedAt
    )

    fun toMemberEntity(groupId: String): MemberEntity{
        return MemberEntity(
            groupId = groupId,
            userId = userId.id,
            role = role.toMemberRole(),
            joinedAt = joinedAt.isoToLong()
        )
    }
}
