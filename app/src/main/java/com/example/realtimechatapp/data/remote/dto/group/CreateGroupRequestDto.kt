package com.example.realtimechatapp.data.remote.dto.group

data class CreateGroupRequestDto (
    val name: String,
    val members: List<String>
)