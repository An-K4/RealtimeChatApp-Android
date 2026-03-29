package com.example.realtimechatapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GetGroupMessageResponseDto(
    val message: String,
    @SerializedName("messages") val groupMessages: List<MessageDto>
)