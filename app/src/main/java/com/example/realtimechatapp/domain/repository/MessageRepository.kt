package com.example.realtimechatapp.domain.repository

import com.example.realtimechatapp.data.remote.dto.MessageDto
import com.example.realtimechatapp.data.remote.dto.MessageSeenDto
import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.model.MessageContact
import com.example.realtimechatapp.domain.model.SendMessageParam
import com.example.realtimechatapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    suspend fun getMessageContacts(): Result<Unit>
    suspend fun getMessage(friendId: String): Result<Unit>
    suspend fun getHeaderInfo(friendId: String): Result<User>
    fun observeMessages(friendId: String): Flow<List<Message>>
    fun observeMessageContacts(): Flow<List<MessageContact>>
    suspend fun sendMessage(message: SendMessageParam)
    suspend fun seenMessage(friendId: String)
    suspend fun markMessageAsSeen(senderId: String, receiverId: String)
}