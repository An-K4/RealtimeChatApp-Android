package com.example.realtimechatapp.domain.model

import com.google.gson.annotations.SerializedName

data class SendGroupMessageParam(
    @SerializedName("groupId") val groupId: String,
    @SerializedName("content") val content: String,
    @SerializedName("replyTo") val replyTo: String? = null,
    @SerializedName("fileUrl") val fileUrl: String? = null
)