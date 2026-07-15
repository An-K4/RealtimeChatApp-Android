package com.example.realtimechatapp.domain.model

import com.example.realtimechatapp.R
import com.example.realtimechatapp.common.UiText

enum class Role(val rawValue: String) {
    OWNER("owner"),
    ADMIN("admin"),
    MEMBER("member");

    fun toDisplayName(): UiText {
        return when (this) {
            OWNER -> UiText.StringResource(R.string.owner)
            ADMIN -> UiText.StringResource(R.string.admin)
            MEMBER -> UiText.StringResource(R.string.member)
        }
    }
}