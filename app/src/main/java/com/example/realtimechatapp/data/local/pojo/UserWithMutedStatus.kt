package com.example.realtimechatapp.data.local.pojo

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.example.realtimechatapp.common.formatToTime
import com.example.realtimechatapp.data.local.entity.UserEntity
import com.example.realtimechatapp.domain.model.User

data class UserWithMutedStatus(
    @Embedded val user: UserEntity,
    @ColumnInfo("is_muted") val isMuted: Boolean
)

fun UserWithMutedStatus.toUser() = User(
    id = this.user.id,
    username = this.user.username,
    fullName = this.user.fullName,
    email = this.user.email,
    avatar = this.user.avatar,
    isMuted = this.isMuted,
    createdAt = this.user.createdAt.formatToTime(toHourMinute = false)
)
