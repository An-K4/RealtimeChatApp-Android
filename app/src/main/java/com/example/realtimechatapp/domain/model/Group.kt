package com.example.realtimechatapp.domain.model

data class Group(
    val id: String,
    val name: String,
    val avatar: String?,
    val description: String?,
    val owner: User,
    val members: List<Member>,
    val isActive: Boolean = true,
    val createdAt: String
)
