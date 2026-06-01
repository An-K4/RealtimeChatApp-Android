package com.example.realtimechatapp.data.remote.dto.group

import com.example.realtimechatapp.data.remote.dto.message.MessageDto
import com.google.gson.annotations.SerializedName

data class GetGroupMessageResponseDto(
    val message: String,
    @SerializedName("messages") val groupMessages: List<MessageDto>
)