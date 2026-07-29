package com.example.realtimechatapp.data.remote.dto.socket

import com.example.realtimechatapp.data.remote.dto.group.MemberDto

data class MemberRoleChangedDto(
    val groupId: String,
    val member: MemberDto
)
