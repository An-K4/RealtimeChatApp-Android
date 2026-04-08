package com.example.realtimechatapp.data.repository

import com.example.realtimechatapp.data.local.manager.TokenManager
import com.example.realtimechatapp.data.remote.SocketEvents
import com.example.realtimechatapp.data.remote.dto.MessageDto
import com.example.realtimechatapp.domain.model.SendMessageParam
import com.example.realtimechatapp.domain.repository.SocketConnectionState
import com.example.realtimechatapp.domain.repository.SocketRepository
import com.google.gson.Gson
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class SocketRepositoryImpl @Inject constructor(
    private val tokenManager: TokenManager,
    private val gson: Gson,
    private val baseUrl: String
): SocketRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var socket: Socket? = null

    private val _messagesFlow = MutableSharedFlow<MessageDto>()
    override fun observeMessages(): Flow<MessageDto> = _messagesFlow.asSharedFlow()
    override fun observeMessageContacts(): Flow<MessageDto> = _messagesFlow.asSharedFlow()

    private val _socketConnectionState = MutableStateFlow<SocketConnectionState>(
        SocketConnectionState.Disconnected
    )
    override fun observeConnectionState(): Flow<SocketConnectionState> = _socketConnectionState.asStateFlow()

    override suspend fun connect() {
        if (socket?.connected() == true){
            Timber.d("Socket đã kết nối trước đó")
            return
        }

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

        socket?.connect()
    }

    override suspend fun disconnect() {
        Timber.d("Ngắt kết nối socket...")
        socket?.let { socket ->
            socket.disconnect()
            socket.off()
            socket.close()
        }
        socket = null
    }

    override suspend fun isConnected(): Boolean = socket?.connected() ?: false

    private fun setupSocketConnectionListener(){
        socket?.on(Socket.EVENT_CONNECT){
            Timber.d(Socket.EVENT_CONNECT)
            _socketConnectionState.value = SocketConnectionState.Connected
        }

        socket?.on(Socket.EVENT_DISCONNECT){
            Timber.e(Socket.EVENT_DISCONNECT)
            _socketConnectionState.value = SocketConnectionState.Disconnected
        }

        socket?.on(Socket.EVENT_CONNECT_ERROR){ args ->
            val error = args.firstOrNull()?.toString() ?: "Unknown Error"
            Timber.e("${Socket.EVENT_CONNECT_ERROR}: $error")
            _socketConnectionState.value = SocketConnectionState.Error(error)
        }
    }

    private fun setupMessageListener(){
        socket?.on(SocketEvents.RECEIVE_MESSAGE){ args ->
            if (args.isNotEmpty()){
                val rawJsonData = args[0].toString()
                Timber.d("Raw json: $rawJsonData")

                try {
                    val messageDto = gson.fromJson(rawJsonData, MessageDto::class.java)

                    scope.launch {
                        _messagesFlow.emit(messageDto)
                        Timber.d("Đã bắn tin nhắn vào flow: $messageDto")
                    }
                } catch (e: Exception) {
                    Timber.d("Parse JSON thất bại: ${e.message}")
                }
            }
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

        socket?.emit(SocketEvents.SEND_MESSAGE, jsonObject, Ack{ args ->
            if (args.isNotEmpty()){
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
}