package com.example.realtimechatapp.domain.repository

import com.example.realtimechatapp.domain.model.UserContact

interface MessageRepository {
    suspend fun getUsers(): Result<List<UserContact>>
}