package com.example.realtimechatapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UploadResponseDto(
    val message: String,
    @SerializedName("url") val url: String
)