package com.example.realtimechatapp.data.remote.dto.group

import com.google.gson.annotations.SerializedName

data class AddMembersRequestDto(@SerializedName("memberIds") val memberIds: List<String>)
