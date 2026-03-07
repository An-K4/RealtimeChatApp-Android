package com.example.realtimechatapp.data.remote.dto

import com.example.realtimechatapp.domain.model.Group
import com.google.gson.annotations.SerializedName

data class GroupDto(
    @SerializedName("_id") val id: String,
    val name: String,
    val avatar: String?,
    val description: String?,
    val owner: UserDto,
    val members: List<MemberDto>,
    val isActive: Boolean,
    val createdAt: String
){
    fun toGroup(): Group {
        return Group(
            id = id,
            name = name,
            avatar = avatar,
            description = description,
            owner = owner.toUser(),
            members = members.map { it.toMember() },
            isActive = isActive,
            createdAt = createdAt
        )
    }
}
