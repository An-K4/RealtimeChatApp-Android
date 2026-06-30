package com.example.realtimechatapp.data.remote.dto.group

import com.google.gson.annotations.SerializedName

data class GetMembersResponseDto(
    val message: String,
    @SerializedName("members") val members: List<MemberDto>
)
