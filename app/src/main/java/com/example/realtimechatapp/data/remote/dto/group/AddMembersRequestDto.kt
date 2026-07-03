package com.example.realtimechatapp.data.remote.dto.group

import kotlinx.serialization.SerialName

data class AddMembersRequestDto(@SerialName("memberIds") val memberIds: List<String>)
