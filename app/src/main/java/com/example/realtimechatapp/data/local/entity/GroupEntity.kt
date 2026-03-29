package com.example.realtimechatapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "groups",
    indices = [Index(value = ["owner_id"])]
)
data class GroupEntity(
    @PrimaryKey
    val id: String,

    val name: String,
    val avatar: String?,
    val description: String?,

    @ColumnInfo(name = "owner_id")
    val ownerId: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
