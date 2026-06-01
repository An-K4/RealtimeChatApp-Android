package com.example.realtimechatapp.data.remote.dto.group

import com.example.realtimechatapp.common.isoToLong
import com.example.realtimechatapp.data.local.entity.GroupEntity
import com.example.realtimechatapp.data.remote.dto.user.UserDto
import com.google.gson.annotations.SerializedName

data class GroupDto(
    @SerializedName("_id") val id: String,
    val name: String,
    val avatar: String?,
    val description: String?,
    val owner: UserDto,
    val members: List<MemberDto>,
    val createdAt: String
) {
    fun toGroupEntity() = GroupEntity(
        id = id,
        name = name,
        avatar = avatar,
        description = description,
        ownerId = owner.id,
        createdAt = createdAt.isoToLong(),
        updatedAt = createdAt.isoToLong()
    )
}
