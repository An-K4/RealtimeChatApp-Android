package com.example.realtimechatapp.domain.repository

import com.example.realtimechatapp.data.remote.dto.GroupMessageSeenDto
import com.example.realtimechatapp.data.remote.dto.MessageDto
import com.example.realtimechatapp.data.remote.dto.MessageSeenDto
import com.example.realtimechatapp.domain.model.SendGroupMessageParam
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

    suspend fun observeGroupMessages(): Flow<MessageDto>
    suspend fun observeGroupMessageContacts(): Flow<MessageDto>
    suspend fun observeGroupMessageSeen(): Flow<GroupMessageSeenDto>

    suspend fun sendGroupMessage(groupMessage: SendGroupMessageParam)
    suspend fun seenGroupMessage(messageSeen: GroupMessageSeenDto)
}

sealed class SocketConnectionState {
    object Connected: SocketConnectionState()
    object Disconnected: SocketConnectionState()
    data class Error(val message: String): SocketConnectionState()
}

object SocketEvents {
    // message
    const val RECEIVE_MESSAGE = "receive-message"
    const val SEND_MESSAGE = "send-message"
    const val SEEN_MESSAGE = "seen-message"
    const val TYPING_START = "typing-start"
    const val TYPING_STOP = "typing-stop"
    const val NOTIFY_USER_ONLINE = "noti-online"
    const val NOTIFY_USER_OFFLINE = "noti-offline"
    const val NOTIFY_ONLINE_LIST = "noti-onlineList-toMe"

    // group
    const val RECEIVE_GROUP_MESSAGE = "receive-group-message"
    const val SEEN_GROUP_MESSAGE = "seen-group-message"
    const val USER_SEEN_MESSAGE = "user-seen-message"
    const val SEND_GROUP_MESSAGE = "send-group-message"
}