package com.example.realtimechatapp.data.remote.dto.socket

import com.example.realtimechatapp.data.remote.dto.group.MemberDto

data class OwnerTransferredDto(
    val groupId: String,
    val newOwner: MemberDto
)
