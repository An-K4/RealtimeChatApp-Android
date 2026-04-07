package com.example.realtimechatapp.domain.repository

import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.model.MessageContact
import com.example.realtimechatapp.domain.model.SendMessageParam
import com.example.realtimechatapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    suspend fun getMessageContacts(): Result<List<MessageContact>>
    suspend fun getMessage(friendId: String): Result<Unit>
    suspend fun getHeaderInfo(friendId: String): Result<User>
    fun observeMessage(friendId: String): Flow<List<Message>>
    suspend fun sendMessage(message: SendMessageParam)
}