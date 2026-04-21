package com.example.realtimechatapp.ui.screens.messages

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.domain.model.Message
import com.example.realtimechatapp.domain.model.User
import com.example.realtimechatapp.domain.usecase.messages.GetHeaderInfoUseCase
import com.example.realtimechatapp.domain.usecase.messages.GetMessageUseCase
import com.example.realtimechatapp.domain.usecase.socket.ObserveMessageUseCase
import com.example.realtimechatapp.domain.usecase.socket.ObserveOnlineUserUseCase
import com.example.realtimechatapp.domain.usecase.socket.SeenMessageUseCase
import com.example.realtimechatapp.domain.usecase.socket.SendMessageUseCase
import com.example.realtimechatapp.domain.usecase.user.GetCurrentUserIdUseCase
import com.example.realtimechatapp.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
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
    private val sendMessageUseCase: SendMessageUseCase,
    private val seenMessageUseCase: SeenMessageUseCase
) : ViewModel() {
    data class DetailMessageState(
        val currentUserId: String = "",
        val friendId: String = "",
        val friendName: String? = null,
        val friendStatus: String? = null,
        val friendAvatar: String? = null,
        val messages: List<Message> = emptyList(),
        val messageInput: String? = null,
        val isLoading: Boolean = false
    )

    sealed class DetailMessageEvent {
        object GetMessageSuccess : DetailMessageEvent()
        data class Failure(val message: String) : DetailMessageEvent()
    }

    private val currentUserId = flow {
        emit(getCurrentUserIdUseCase())
    }
    private val friendId: String = checkNotNull(
        savedStateHandle[Screen.DetailMessage.ARG_FRIEND_ID]
    )
    private val _messageInput = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(true)
    private val _headerInfo = MutableStateFlow<User?>(null)

    val detailMessageState = combine(
        currentUserId,
        _headerInfo,
        observeMessageUseCase(friendId),
        observeOnlineUserUseCase(),
        _messageInput
    ){ currentUserId, friendUser, messages, onlineUserIds, messageInput ->
        DetailMessageState(
            currentUserId = currentUserId,
            friendId = friendId,
            friendName = friendUser?.fullName,
            friendStatus = if (onlineUserIds.contains(friendId)) "Đang hoạt động" else "Ngoại tuyến",
            friendAvatar = friendUser?.avatar,
            messages = messages,
            messageInput = messageInput,
            isLoading = _isLoading.value && messages.isEmpty()
        )
    }.catch { exception ->
        Timber.e("Lỗi luồng màn hình nhắn chi tiết: ${exception.getErrorMessage()}")
        _detailMessageEvent.send(DetailMessageEvent.Failure(exception.getErrorMessage()))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DetailMessageState(isLoading = true)
    )

    fun onMessageInputChange(newValue: String) {
        _messageInput.value = newValue
    }

    private val _detailMessageEvent = Channel<DetailMessageEvent>()
    val detailMessageEvent = _detailMessageEvent.receiveAsFlow()

    // init after state variables
    init {
        getHeaderInfo()
        getMessages()
        markMessageAsSeen()
    }

    fun getHeaderInfo() {
        viewModelScope.launch {

            val result = getHeaderInfoUseCase(friendId)

            result.onSuccess { user ->
                _headerInfo.value = user
                Timber.d("Thông tin người dùng: ${user.toString()}")
            }.onFailure { e ->
                _detailMessageEvent.send(DetailMessageEvent.Failure(e.getErrorMessage()))
            }
        }
    }

    fun getMessages() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = getMessageUseCase(friendId)

            result.onSuccess {
                // _detailMessageEvent.send(DetailMessageEvent.GetMessageSuccess)
                Timber.d("Lấy tin nhắn thành công")
            }.onFailure { e ->
                _detailMessageEvent.send(DetailMessageEvent.Failure(e.getErrorMessage()))
            }

            _isLoading.value = false
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

            _messageInput.value = ""
        }
    }

    fun markMessageAsSeen() {
        viewModelScope.launch {
            seenMessageUseCase(friendId)
        }
    }
}
