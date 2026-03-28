package com.example.realtimechatapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.realtimechatapp.common.formatToTime
import com.example.realtimechatapp.domain.model.User

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["email"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey
    val id: String,

    val username: String,
    val fullName: String,
    val email: String,
    val avatar: String?,

    @ColumnInfo(name = "is_online")
    val isOnline: Boolean = false,

    @ColumnInfo(name = "last_seen")
    val lastSeen: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

fun UserEntity.toUser() = User(
        id = this.id,
        username = this.username,
        fullName = this.fullName,
        email = this.email,
        avatar = this.avatar,
        createdAt = this.createdAt.formatToTime(false)
)
