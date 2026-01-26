package com.example.realtimechatapp.ui.screens.auth

import com.example.realtimechatapp.domain.model.User

data class LoginState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null
)
