package com.example.realtimechatapp.data.remote.dto.user

import com.example.realtimechatapp.data.remote.dto.group.GroupDto
import kotlinx.serialization.SerialName

data class SearchResponseDto(
    @SerialName("message") val message: String,
    @SerialName("users") val users: List<UserDto>,
    @SerialName("groups") val groups: List<GroupDto>,
)
