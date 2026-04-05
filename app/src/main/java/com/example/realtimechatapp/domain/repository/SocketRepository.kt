package com.example.realtimechatapp.domain.repository

import com.example.realtimechatapp.data.remote.dto.MessageDto
import com.example.realtimechatapp.domain.model.SendMessageParam
import kotlinx.coroutines.flow.Flow

interface SocketRepository {
    suspend fun connect()
    suspend fun disconnect()
    suspend fun isConnected(): Boolean

    fun observeMessages(): Flow<MessageDto>
    fun observeConnectionState(): Flow<SocketConnectionState>

    suspend fun sendMessage(message: SendMessageParam)
}

sealed class SocketConnectionState {
    object Connected: SocketConnectionState()
    object Disconnected: SocketConnectionState()
    data class Error(val message: String): SocketConnectionState()
}
