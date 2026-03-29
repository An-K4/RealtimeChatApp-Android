package com.example.realtimechatapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "members",
    primaryKeys = ["group_id", "user_id"],
    indices = [Index(value = ["user_id"])]
)
data class MemberEntity(
    @ColumnInfo(name = "group_id")
    val groupId: String,

    @ColumnInfo(name = "user_id")
    val userId: String,

    val role: ParticipantRole,

    @ColumnInfo(name = "joined_at")
    val joinedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "last_read_timestamp")
    val lastReadTimestamp: Long = System.currentTimeMillis()
)

enum class ParticipantRole{
    OWNER, ADMIN, MEMBER
}