package com.example.realtimechatapp.data.remote.dto.group

import com.example.realtimechatapp.data.local.entity.MemberRole
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