package com.example.realtimechatapp.data.remote.dto.group

data class UpdateGroupRequestDto(
    val name: String,
    val avatar: String?,
    val description: String?
)
