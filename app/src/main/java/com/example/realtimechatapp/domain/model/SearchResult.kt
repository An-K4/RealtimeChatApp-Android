package com.example.realtimechatapp.domain.model

data class SearchResult(
    val users: List<User>? = null,
    val groups: List<Group>? = null
)
