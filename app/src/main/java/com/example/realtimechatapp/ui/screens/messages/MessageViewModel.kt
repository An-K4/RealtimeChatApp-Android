package com.example.realtimechatapp.ui.screens.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimechatapp.common.UiText
import com.example.realtimechatapp.common.getErrorMessage
import com.example.realtimechatapp.data.local.manager.TokenManagerImpl
import com.example.realtimechatapp.domain.exception.NetworkException
import com.example.realtimechatapp.domain.model.MessageContact
import com.example.realtimechatapp.domain.usecase.message.GetMessageContactUseCase
import com.example.realtimechatapp.domain.usecase.socket.ConnectSocketUseCase
import com.example.realtimechatapp.domain.usecase.socket.message.ObserveMessageContactUseCase
import com.example.realtimechatapp.domain.usecase.socket.message.ObserveOnlineUserUseCase
import com.example.realtimechatapp.domain.usecase.socket.message.ObserveTypingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val getMessageContactUseCase: GetMessageContactUseCase,
    private val observeMessageContactUseCase: ObserveMessageContactUseCase,
    private val observeOnlineUserUseCase: ObserveOnlineUserUseCase,
    private val observeTypingUseCase: ObserveTypingUseCase,
    private val connectSocketUseCase: ConnectSocketUseCase,
    private val tokenManager: TokenManagerImpl
) : ViewModel() {
    data class MessageState(
        val messageDialogState: MessageDialogState = MessageDialogState.Dismiss,
        val isLoading: Boolean = false,
        val users: List<MessageContact> = emptyList()
    )

    sealed interface MessageDialogState {
        object Dismiss : MessageDialogState
        object Unauthenticated : MessageDialogState
        data class Failure(val message: UiText) : MessageDialogState
    }

    private val _isLoading = MutableStateFlow(false)
    private val _messageDialogState = MutableStateFlow<MessageDialogState>(MessageDialogState.Dismiss)
    val messageState = combine(
        observeMessageContactUseCase().catch { exception ->
            Timber.e("Lỗi luồng lấy danh sách người dùng: ${exception.getErrorMessage()}")
            emit(emptyList())
        },
        observeOnlineUserUseCase().catch { exception ->
            Timber.e("Lỗi luồng user online: ${exception.getErrorMessage()}")
            emit(emptySet())
        },
        observeTypingUseCase().catch { exception ->
            Timber.e("Lỗi luồng user typing: ${exception.getErrorMessage()}")
            emit(emptySet())
        },
        _messageDialogState,
        _isLoading
    ) { messageContacts, onlineUserIds, typingUserIds, dialogState, isLoading ->
        MessageState(
            messageDialogState = dialogState,
            isLoading = isLoading && messageContacts.isEmpty(),
            users = messageContacts.map { contact ->
                contact.copy(
                    isOnline = onlineUserIds.contains(contact.id),
                    isTyping = typingUserIds.contains(contact.id)
                )
            }
        )
    }.catch { exception ->
        if (exception is CancellationException) throw exception
        Timber.e("Lỗi luồng màn hình tin nhắn: ${exception.getErrorMessage()}")
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MessageState(isLoading = true)
    )

    init {
        viewModelScope.launch {
            if (isTokenValid()) {
                connectSocket()
                getUsers() // auto load
            }
        }
    }

    private suspend fun isTokenValid(): Boolean {
        val token = tokenManager.token.firstOrNull()
        return if (token.isNullOrEmpty()) {
            _messageDialogState.value = MessageDialogState.Unauthenticated
            false
        } else {
            true
        }
    }

    fun connectSocket() {
        viewModelScope.launch {
            connectSocketUseCase()
        }
    }

    fun getUsers() {
        viewModelScope.launch {
            _isLoading.value = true

            getMessageContactUseCase().onSuccess {
                // do nothing, delegate to observe fun
            }.onFailure { exception ->
                when (exception) {
                    is NetworkException.NoInternetException,
                    NetworkException.ServerUnreachableException -> {
                        Timber.e("${exception.getErrorMessage()}")
                    }

                    else -> _messageDialogState.value = MessageDialogState.Failure(exception.getErrorMessage())
                }
            }

            _isLoading.value = false
        }
    }

    fun dismissDialog() {
        _messageDialogState.value = MessageDialogState.Dismiss
    }
}
