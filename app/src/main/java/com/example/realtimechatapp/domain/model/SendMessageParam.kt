package com.example.realtimechatapp.domain.model

import com.google.gson.annotations.SerializedName

data class SendMessageParam(
    @SerializedName("content") val content: String,
    @SerializedName("receiverId") val receiverId: String,
    @SerializedName("replyTo") val replyTo: String? = null,
    @SerializedName("fileUrl") val fileUrl: String? = null
)