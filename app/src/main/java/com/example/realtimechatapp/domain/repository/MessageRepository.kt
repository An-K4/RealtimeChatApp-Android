package com.example.realtimechatapp.domain.repository

import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.model.UserContact

interface MessageRepository {
    suspend fun getUsers(): Result<List<UserContact>>
    suspend fun getMessage(friendId: String): Result<List<Message>>
}