package com.example.realtimechatapp.domain.repository

import com.example.realtimechatapp.data.remote.dto.MessageDto
import com.example.realtimechatapp.data.remote.dto.MessageSeenDto
import com.example.realtimechatapp.domain.model.SendMessageParam
import kotlinx.coroutines.flow.Flow

interface SocketRepository {
    suspend fun connect()
    suspend fun disconnect()
    suspend fun isConnected(): Boolean

    fun observeMessages(): Flow<MessageDto>
    fun observeMessageContacts(): Flow<MessageDto>
    fun observeMessageSeen(): Flow<MessageSeenDto>
    fun observeConnectionState(): Flow<SocketConnectionState>
    fun observeOnlineUserIds(): Flow<Set<String>>
    fun observeTypingStatus(): Flow<Set<String>>

    suspend fun sendMessage(message: SendMessageParam)
    suspend fun seenMessage(messageSeen: MessageSeenDto)
    suspend fun emitTypingStart(receiverId: String)
    suspend fun emitTypingStop(receiverId: String)
}

sealed class SocketConnectionState {
    object Connected: SocketConnectionState()
    object Disconnected: SocketConnectionState()
    data class Error(val message: String): SocketConnectionState()
}
