package com.example.realtimechatapp.data.local.pojo

import androidx.room.Embedded
import androidx.room.Relation
import com.example.realtimechatapp.common.formatToTime
import com.example.realtimechatapp.data.local.entity.MemberEntity
import com.example.realtimechatapp.data.local.entity.UserEntity
import com.example.realtimechatapp.data.local.entity.toRole
import com.example.realtimechatapp.data.local.entity.toUser
import com.example.realtimechatapp.domain.model.Member

class MemberWithDetails(
    @Embedded val member: MemberEntity,

    @Relation(
        parentColumn = "user_id",
        entityColumn = "id"
    )
    val user: UserEntity
)

fun MemberWithDetails.toMember() = Member(
    userId = user.toUser(),
    role = member.toRole(),
    joinedAt = member.joinedAt.formatToTime(false)
)

