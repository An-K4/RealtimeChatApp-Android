package com.example.realtimechatapp.data.remote.dto.group

data class GroupInfoUpdatedDto(
    val id: String,
    val name: String,
    val avatar: String?,
    val description: String?
)
