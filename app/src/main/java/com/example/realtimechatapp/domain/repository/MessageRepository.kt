package com.example.realtimechatapp.domain.repository

import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.model.MessageContact

interface MessageRepository {
    suspend fun getUsers(): Result<List<MessageContact>>
    suspend fun getMessage(friendId: String): Result<List<Message>>
}