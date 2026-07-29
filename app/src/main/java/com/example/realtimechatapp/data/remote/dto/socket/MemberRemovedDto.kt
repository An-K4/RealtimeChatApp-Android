package com.example.realtimechatapp.data.remote.dto.socket

data class MemberRemovedDto(
    val groupId: String,
    val groupName: String,
    val groupAvatar: String,
    val removedBy: String,
    val removedAt: String
)
