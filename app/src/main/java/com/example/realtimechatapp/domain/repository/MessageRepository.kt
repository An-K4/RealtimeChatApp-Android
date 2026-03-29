package com.example.realtimechatapp.domain.repository

import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.model.MessageContact
import com.example.realtimechatapp.domain.model.User

interface MessageRepository {
    suspend fun getMessageContacts(): Result<List<MessageContact>>
    suspend fun getMessage(friendId: String): Result<List<Message>>
    suspend fun getHeaderInfo(userId: String): Result<User>
}