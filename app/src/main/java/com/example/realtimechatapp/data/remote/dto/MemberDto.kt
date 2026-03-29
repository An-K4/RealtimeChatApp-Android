package com.example.realtimechatapp.data.remote.dto

import com.example.realtimechatapp.common.isoToLong
import com.example.realtimechatapp.data.local.entity.MemberEntity
import com.example.realtimechatapp.data.local.entity.MemberRole
import com.google.gson.annotations.SerializedName

enum class RoleDto{
    @SerializedName("owner") OWNER,
    @SerializedName("admin") ADMIN,
    @SerializedName("member") MEMBER;

    fun toMemberRole(): MemberRole{
        return when(this){
            OWNER -> MemberRole.OWNER
            ADMIN -> MemberRole.ADMIN
            MEMBER -> MemberRole.MEMBER
        }
    }
}

data class MemberDto(
    val userId: UserDto,
    val role: RoleDto,
    val joinedAt: String
){
    fun toMemberEntity(groupId: String): MemberEntity{
        return MemberEntity(
            groupId = groupId,
            userId = userId.id,
            role = role.toMemberRole(),
            joinedAt = joinedAt.isoToLong()
        )
    }
}
