package com.example.realtimechatapp.ui.screens.messages

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.R
import com.example.realtimechatapp.common.UiText
import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.model.User
import com.example.realtimechatapp.domain.usecase.messages.GetHeaderInfoUseCase
import com.example.realtimechatapp.domain.usecase.messages.GetMessageUseCase
import com.example.realtimechatapp.domain.usecase.socket.EmitTypingStartUseCase
import com.example.realtimechatapp.domain.usecase.socket.EmitTypingStopUseCase
import com.example.realtimechatapp.domain.usecase.socket.ObserveMessageUseCase
import com.example.realtimechatapp.domain.usecase.socket.ObserveOnlineUserUseCase
import com.example.realtimechatapp.domain.usecase.socket.ObserveTypingUseCase
import com.example.realtimechatapp.domain.usecase.socket.SeenMessageUseCase
import com.example.realtimechatapp.domain.usecase.socket.SendMessageUseCase
import com.example.realtimechatapp.domain.usecase.user.GetCurrentUserIdUseCase
import com.example.realtimechatapp.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DetailMessageViewModel @Inject constructor(
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val getMessageUseCase: GetMessageUseCase,
    private val getHeaderInfoUseCase: GetHeaderInfoUseCase,
    private val observeMessageUseCase: ObserveMessageUseCase,
    private val observeOnlineUserUseCase: ObserveOnlineUserUseCase,
    private val observeTypingUseCase: ObserveTypingUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val seenMessageUseCase: SeenMessageUseCase,
    private val emitTypingStartUseCase: EmitTypingStartUseCase,
    private val emitTypingStopUseCase: EmitTypingStopUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {
    data class DetailMessageState(
        val currentUserId: String = "",
        val friendId: String = "",
        val friendName: String? = null,
        val friendStatus: String? = null,
        val friendTypingStatus: Boolean = false,
        val friendAvatar: String? = null,
        val messages: List<Message> = emptyList(),
        val messageInput: String? = null,
        val isLoading: Boolean = false
    )

    sealed class DetailMessageEvent {
        object GetMessageSuccess : DetailMessageEvent()
        data class Failure(val message: String) : DetailMessageEvent()
    }

    private data class DetailMessageContext(
        val currentUserId: String,
        val friendUser: User?,
    )

    private data class SocketData(
        val messages: List<Message>,
        val onlineUserIds: Set<String>,
        val typingUserIds: Set<String>
    )

    private data class InputAndLoadingState(
        val messageInput: String,
        val isLoading: Boolean
    )

    private val currentUserId = flow { emit(getCurrentUserIdUseCase()) }.catch { exception ->
        Timber.e(exception, "Lỗi lấy id người dùng hiện tại")
    }
    private val friendId: String =
        checkNotNull(savedStateHandle[Screen.DetailMessage.ARG_FRIEND_ID])
    private val _messageInput = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(true)
    private val _headerInfo = MutableStateFlow<User?>(null)
    private val detailMessageContextFlow =
        combine(currentUserId, _headerInfo) { currentUserId, headerInfo ->
            DetailMessageContext(
                currentUserId = currentUserId,
                friendUser = headerInfo
            )
        }

    private val socketDataFlow = combine(
        observeMessageUseCase(friendId).catch { exception ->
            Timber.e(exception, "Lỗi luồng DB Message")
            emit(emptyList())
        },
        observeOnlineUserUseCase().catch { exception ->
            Timber.e(exception, "Lỗi luồng user online")
            emit(emptySet())
        },
        observeTypingUseCase().catch { exception ->
            Timber.e(exception, "Lỗi luồng user typing")
            emit(emptySet())
        }
    ) { messages, onlineUserIds, typingUserIds ->
        SocketData(
            messages = messages,
            onlineUserIds = onlineUserIds,
            typingUserIds = typingUserIds
        )
    }

    // mutable state flows don't throw exception
    private val inputAndLoadingStateFlow = combine(
        _messageInput,
        _isLoading
    ) { messageInput, isLoading ->
        InputAndLoadingState(
            messageInput = messageInput,
            isLoading = isLoading
        )
    }

    val detailMessageState = combine(
        detailMessageContextFlow,
        socketDataFlow,
        inputAndLoadingStateFlow
    ) { detailMessageContext, socketData, inputAndLoadingState ->
        DetailMessageState(
            currentUserId = detailMessageContext.currentUserId,
            friendId = friendId,
            friendName = detailMessageContext.friendUser?.fullName ?: "",
            friendStatus = if (socketData.onlineUserIds.contains(friendId))
                UiText.StringResource(R.string.online).asString(context)
            else
                UiText.StringResource(R.string.offline).asString(context),
            friendTypingStatus = socketData.typingUserIds.contains(friendId),
            friendAvatar = detailMessageContext.friendUser?.avatar ?: "",
            messages = socketData.messages,
            messageInput = inputAndLoadingState.messageInput,
            isLoading = inputAndLoadingState.isLoading && socketData.messages.isEmpty()
        )
    }.catch { exception ->
        Timber.e(exception, "Lỗi luồng màn hình nhắn chi tiết")
        _detailMessageEvent.send(DetailMessageEvent.Failure(exception.getErrorMessage().asString(context)))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DetailMessageState(isLoading = true)
    )

    private val _detailMessageEvent = Channel<DetailMessageEvent>()
    val detailMessageEvent = _detailMessageEvent.receiveAsFlow()

    // init after state variables
    init {
        getHeaderInfo()
        getMessages()
        markMessageAsSeen()
    }

    private fun getHeaderInfo() {
        viewModelScope.launch {
            val result = getHeaderInfoUseCase(friendId)

            result.onSuccess { user ->
                _headerInfo.value = user
                Timber.d("Thông tin người dùng: $user")
            }.onFailure { e ->
                _detailMessageEvent.send(DetailMessageEvent.Failure(e.getErrorMessage().asString(context)))
            }
        }
    }

    private fun getMessages() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = getMessageUseCase(friendId)

            result.onSuccess {
                // _detailMessageEvent.send(DetailMessageEvent.GetMessageSuccess)
                Timber.d("Lấy tin nhắn thành công")
            }.onFailure { e ->
                _detailMessageEvent.send(DetailMessageEvent.Failure(e.getErrorMessage().asString(context)))
            }

            _isLoading.value = false
        }
    }

    fun markMessageAsSeen() {
        viewModelScope.launch {
            seenMessageUseCase(friendId)
        }
    }

    private var typingJob: Job? = null
    fun onMessageInputChange(newValue: String) {
        _messageInput.value = newValue

        if (newValue.isEmpty()) {
            if (typingJob?.isActive == true) {
                // clear job and return
                typingJob?.cancel()
                typingJob = null
                viewModelScope.launch { emitTypingStopUseCase(friendId) }
            }
            return
        } else {
            if (typingJob?.isActive != true) {
                viewModelScope.launch { emitTypingStartUseCase(friendId) }
            }

            // reset old timer
            typingJob?.cancel()
            typingJob = viewModelScope.launch {
                delay(3000)
                emitTypingStopUseCase(friendId)
            }
        }
    }

    fun sendMessage() {
        val content = _messageInput.value.trim()
        if (content.isEmpty()) return

        viewModelScope.launch {
            sendMessageUseCase(
                content = content,
                receiverId = friendId
            )

            typingJob?.cancel()
            typingJob = null
            viewModelScope.launch { emitTypingStopUseCase(friendId) }

            _messageInput.value = ""
        }
    }
}
