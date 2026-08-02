package com.example.realtimechatapp.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

data class UpdateFcmTokenRequest(
    @SerializedName("fcmToken")
    val fcmToken: String
)
