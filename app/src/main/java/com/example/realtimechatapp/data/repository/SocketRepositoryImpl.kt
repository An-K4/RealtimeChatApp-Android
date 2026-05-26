package com.example.realtimechatapp.data.repository

import com.example.realtimechatapp.data.remote.dto.GroupMessageSeenDto
import com.example.realtimechatapp.domain.repository.SocketEvents
import com.example.realtimechatapp.data.remote.dto.MessageDto
import com.example.realtimechatapp.data.remote.dto.MessageSeenDto
import com.example.realtimechatapp.domain.model.SendGroupMessageParam
import com.example.realtimechatapp.domain.model.SendMessageParam
import com.example.realtimechatapp.domain.repository.SocketConnectionState
import com.example.realtimechatapp.domain.repository.SocketRepository
import com.example.realtimechatapp.domain.repository.TokenManager
import com.google.gson.Gson
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class SocketRepositoryImpl @Inject constructor(
    private val tokenManager: TokenManager,
    private val gson: Gson,
    private val baseUrl: String
) : SocketRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var socket: Socket? = null

    private val _messagesFlow = MutableSharedFlow<MessageDto>()
    override fun observeMessages(): Flow<MessageDto> = _messagesFlow.asSharedFlow()
    override fun observeMessageContacts(): Flow<MessageDto> = _messagesFlow.asSharedFlow()

    private val _groupMessageFlow = MutableSharedFlow<MessageDto>()
    override suspend fun observeGroupMessages(): Flow<MessageDto> = _groupMessageFlow.asSharedFlow()
    override suspend fun observeGroupMessageContacts(): Flow<MessageDto> =
        _groupMessageFlow.asSharedFlow()

    private val _messageSeenFlow = MutableSharedFlow<MessageSeenDto>()
    override fun observeMessageSeen(): Flow<MessageSeenDto> = _messageSeenFlow.asSharedFlow()

    private val _groupMessageSeenFlow = MutableSharedFlow<GroupMessageSeenDto>()
    override suspend fun observeGroupMessageSeen(): Flow<GroupMessageSeenDto> = _groupMessageSeenFlow.asSharedFlow()

    // each id in set is unique
    private val _onlineUserIds = MutableStateFlow<Set<String>>(emptySet())
    override fun observeOnlineUserIds(): Flow<Set<String>> = _onlineUserIds.asStateFlow()

    private val _typingUserIds = MutableStateFlow<Set<String>>(emptySet())
    override fun observeTypingStatus(): Flow<Set<String>> = _typingUserIds.asStateFlow()

    private val _socketConnectionState = MutableStateFlow<SocketConnectionState>(
        SocketConnectionState.Disconnected
    )

    override fun observeConnectionState(): Flow<SocketConnectionState> =
        _socketConnectionState.asStateFlow()

    override suspend fun connect() {
        if (socket?.connected() == true) {
            Timber.d("Socket đã kết nối trước đó")
            return
        }

        // hilt can't get token asynchronously in @Provides
        val token = tokenManager.token.first()
        if (token?.isEmpty() == true) return

        val options = IO.Options.builder()
            .setAuth(mapOf("token" to token))
            .setForceNew(true)
            .setReconnection(true)
            .build()

        socket = IO.socket(baseUrl, options)

        setupSocketConnectionListener()
        setupMessageListener()
        setupGroupMessageListener()
        setupOnlineUserIdsListener()
        setupTypingUserIdsListener()

        socket?.connect()
    }

    override suspend fun disconnect() {
        Timber.d("Ngắt kết nối socket...")
        _onlineUserIds.value = emptySet()
        _typingUserIds.value = emptySet()

        socket?.let { socket ->
            socket.disconnect()
            socket.off()
            socket.close()
        }
        socket = null
    }

    override suspend fun isConnected(): Boolean = socket?.connected() ?: false

    private fun setupSocketConnectionListener() {
        socket?.on(Socket.EVENT_CONNECT) {
            Timber.d(Socket.EVENT_CONNECT)
            _socketConnectionState.value = SocketConnectionState.Connected
        }

        socket?.on(Socket.EVENT_DISCONNECT) {
            _onlineUserIds.value = emptySet()
            _typingUserIds.value = emptySet()

            Timber.e(Socket.EVENT_DISCONNECT)
            _socketConnectionState.value = SocketConnectionState.Disconnected
        }

        socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
            val error = args.firstOrNull()?.toString() ?: "Unknown Error"

            if (error.contains("401") || error.contains("unauthorized")) {
                scope.launch {
                    tokenManager.deleteToken()
                    socket?.disconnect()
                }
            }

            Timber.e("${Socket.EVENT_CONNECT_ERROR}: $error, disconnect right now!")
            _socketConnectionState.value = SocketConnectionState.Error(error)
        }
    }

    override fun observeSocketConnectionState(): Flow<Boolean> = callbackFlow {
        val onConnect = Emitter.Listener {
            trySend(true)
        }
        val onDisconnect = Emitter.Listener {
            trySend(false)
        }

        socket?.on(Socket.EVENT_CONNECT, onConnect)
        socket?.on(Socket.EVENT_DISCONNECT, onDisconnect)

        trySend(socket?.connected() == true)

        awaitClose {
            socket?.off(Socket.EVENT_CONNECT, onConnect)
            socket?.off(Socket.EVENT_DISCONNECT, onDisconnect)
        }
    }

    private fun setupMessageListener() {
        socket?.on(SocketEvents.RECEIVE_MESSAGE) { args ->
            if (args.isNotEmpty()) {
                val rawJsonData = args[0].toString()
                Timber.d("Raw json: $rawJsonData")

                try {
                    val messageDto = gson.fromJson(rawJsonData, MessageDto::class.java)

                    scope.launch {
                        _messagesFlow.emit(messageDto)
                        Timber.d("Đã bắn tin nhắn vào flow: $messageDto")
                    }
                } catch (e: Exception) {
                    Timber.e("Parse JSON thất bại: ${e.message}")
                }
            }
        }

        socket?.on(SocketEvents.SEEN_MESSAGE) { args ->
            if (args.isNotEmpty()) {
                val rawJsonData = args[0].toString()
                Timber.d("Raw json: $rawJsonData")

                try {
                    val messageSeenDto = gson.fromJson(rawJsonData, MessageSeenDto::class.java)

                    scope.launch {
                        _messageSeenFlow.emit(messageSeenDto)
                        Timber.d("Đã bắn dữ liệu đã đọc vào flow: $messageSeenDto")
                    }
                } catch (e: Exception) {
                    Timber.e("Parse JSON thất bại: ${e.message}")
                }
            }
        }
    }

    private fun setupOnlineUserIdsListener() {
        socket?.on(SocketEvents.NOTIFY_ONLINE_LIST) { args ->
            val data = args[0] as JSONArray
            val ids = mutableSetOf<String>()

            for (i in 0 until data.length()) {
                ids.add(data.getString(i))
            }
            _onlineUserIds.value = ids
        }

        socket?.on(SocketEvents.NOTIFY_USER_ONLINE) { args ->
            val data = args[0] as JSONObject
            val id = data.getString("id")

            _onlineUserIds.update { it + id }
        }

        socket?.on(SocketEvents.NOTIFY_USER_OFFLINE) { args ->
            val data = args[0] as JSONObject
            val id = data.getString("id")

            _onlineUserIds.update { it - id }
        }
    }

    private fun setupTypingUserIdsListener() {
        socket?.on(SocketEvents.TYPING_START) { args ->
            val data = args[0] as JSONObject
            val id = data.getString("senderId")

            _typingUserIds.update { it + id }
        }

        socket?.on(SocketEvents.TYPING_STOP) { args ->
            val data = args[0] as JSONObject
            val id = data.getString("senderId")

            _typingUserIds.update { it - id }
        }
    }

    override suspend fun sendMessage(message: SendMessageParam) {
//        val jsonObject = JSONObject().apply {
//            put("content", message.content)
//            put("receiverId", message.receiverId)
//            put("replyTo", message.replyTo)
//            put("fileUrl", message.fileUrl)
//        }

        val jsonString = gson.toJson(message)
        val jsonObject = JSONObject(jsonString)

        socket?.emit(SocketEvents.SEND_MESSAGE, jsonObject, Ack { args ->
            if (args.isNotEmpty()) {
                val response = args[0] as JSONObject
                val isSuccess = response.getBoolean("success")

                if (isSuccess) {
                    Timber.d("Gửi tin nhắn thành công")
                    val savedMessageJson = response.getJSONObject("data").toString()
                    val messageDto = gson.fromJson(savedMessageJson, MessageDto::class.java)

                    scope.launch {
                        Timber.d(savedMessageJson)
                        _messagesFlow.emit(messageDto)
                    }
                } else {
                    val errorMessage = response.optString("message", "Lỗi không xác định")
                    Timber.d("Gửi tin nhắn thất bại: $errorMessage")
                }
            }
        })
    }

    override suspend fun seenMessage(messageSeen: MessageSeenDto) {
        val jsonString = gson.toJson(messageSeen)
        val jsonObject = JSONObject(jsonString)

        socket?.emit(SocketEvents.SEEN_MESSAGE, jsonObject)
    }

    override suspend fun emitTypingStart(receiverId: String) {
        val jsonObject = JSONObject().apply { put("receiverId", receiverId) }
        socket?.emit(SocketEvents.TYPING_START, jsonObject)
    }

    override suspend fun emitTypingStop(receiverId: String) {
        // map conversion (shorter, but creates a middle-man map)
        // val data = JSONObject(mapOf("receiverId" to receiverId))

        // direct put (standard)
        val jsonObject = JSONObject().apply { put("receiverId", receiverId) }
        socket?.emit(SocketEvents.TYPING_STOP, jsonObject)
    }

    override fun joinGroup(groupId: String) {
        socket?.emit(SocketEvents.JOIN_GROUP, groupId)
    }

    private fun setupGroupMessageListener() {
        socket?.on(SocketEvents.RECEIVE_GROUP_MESSAGE) { args ->
            val rawJson = args[0].toString()

            try {
                val messageDto = gson.fromJson(rawJson, MessageDto::class.java)

                scope.launch {
                    _groupMessageFlow.emit(messageDto)
                    Timber.d("Đã nhận tin nhắn nhóm: $messageDto")
                }
            } catch (e: Exception) {
                Timber.e(e, "Parse json thất bại")
            }
        }

        socket?.on(SocketEvents.USER_SEEN_MESSAGE) { args ->
            val rawData = args[0].toString()

            scope.launch {
                val groupMessageSeenDto = gson.fromJson(rawData, GroupMessageSeenDto::class.java)
                _groupMessageSeenFlow.emit(groupMessageSeenDto)
            }
        }
    }

    override suspend fun sendGroupMessage(groupMessage: SendGroupMessageParam) {
        val jsonString = gson.toJson(groupMessage)
        val jsonObject = JSONObject(jsonString)

        socket?.emit(SocketEvents.SEND_GROUP_MESSAGE, jsonObject, Ack { args ->
            if (args.isNotEmpty()) {
                val response = args[0] as JSONObject
                val success = response.getBoolean("success")

                if (success) {
                    Timber.d("Gửi tin nhắn nhóm thành công")
                    val savedMessageJson = response.getString("data").toString()
                    val messageDto = gson.fromJson(savedMessageJson, MessageDto::class.java)

                    scope.launch {
                        Timber.d(savedMessageJson)
                        _groupMessageFlow.emit(messageDto)
                    }
                } else {
                    val errorMessage = response.optString("message", "Lỗi không xác định")
                    Timber.d("Gửi tin nhắn nhóm thất bại: $errorMessage")
                }
            }
        })
    }

    override suspend fun seenGroupMessage(messageSeen: GroupMessageSeenDto) {
        val jsonString = gson.toJson(messageSeen)
        val jsonObject = JSONObject(jsonString)

        socket?.emit(SocketEvents.SEEN_GROUP_MESSAGE, jsonObject)
    }
}