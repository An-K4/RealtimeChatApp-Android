package com.example.realtimechatapp.data.local.pojo

import androidx.room.Embedded
import androidx.room.Relation
import com.example.realtimechatapp.common.formatToTime
import com.example.realtimechatapp.data.local.entity.GroupEntity
import com.example.realtimechatapp.data.local.entity.MemberEntity
import com.example.realtimechatapp.data.local.entity.UserEntity
import com.example.realtimechatapp.data.local.entity.toUser
import com.example.realtimechatapp.domain.model.Group

data class GroupWithDetails(
    @Embedded val group: GroupEntity,

    // nested pojo
    @Relation(
        entity = MemberEntity::class,
        parentColumn = "id",
        entityColumn = "group_id"
    )
    val members: List<MemberWithDetails>,

    @Relation(
        parentColumn = "owner_id",
        entityColumn = "id"
    )
    val owner: UserEntity,
)

fun GroupWithDetails.toGroup() = Group(
    id = group.id,
    name = group.name,
    avatar = group.avatar,
    description = group.description,
    owner = owner.toUser(),
    members = members.map { it.toMember() },
    createdAt = group.createdAt.formatToTime(false)
)